package com.jjak0b.android.trackingmypantry.util;

import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.api.Status;

import java.util.function.Function;

public class ResourceUtils {

    public static class ResourcePairLiveData<A, B> extends PairLiveData<Resource<A>, Resource<B>> {

        public ResourcePairLiveData() {
            super(Resource.loading(null), Resource.loading(null));
        }

        public static <A, B> ResourcePairLiveData<A, B> create(LiveData<Resource<A>> ldA, LiveData<Resource<B>> ldB) {
            ResourcePairLiveData<A, B> o = new ResourcePairLiveData<>();
            o.addSources(ldA, ldB);
            return o;
        }

        public void addSources(LiveData<Resource<A>> ldA, LiveData<Resource<B>> ldB ) {
            addSources(
                    ldA,
                    ldB,
                    bResource -> bResource != null && bResource.getStatus() != Status.LOADING,
                    aResource -> aResource != null && aResource.getStatus() != Status.LOADING
            );
        }

        public LiveData<Resource<Pair<A, B>>> asCombinedLiveData() {
            final MediatorLiveData<Resource<Pair<A, B>>> mediator = new MediatorLiveData<>();

            mediator.addSource(this, pair -> {
                if( pair.first.getStatus() != Status.LOADING
                && pair.second.getStatus() != Status.LOADING ) {
                    mediator.removeSource(this);

                    if( pair.first.getStatus() == Status.SUCCESS
                        && pair.second.getStatus() == Status.SUCCESS ) {
                        mediator.setValue(Resource.success(Pair.create(
                                pair.first.getData(), pair.second.getData()
                        )));
                    }
                    else {
                        mediator.setValue(Resource.error(
                                pair.first.getStatus() == Status.ERROR ? pair.first.getError() : pair.second.getError(),
                                Pair.create(pair.first.getData(), pair.second.getData())
                        ));
                    }
                }
                else {
                    mediator.setValue(Resource.loading(
                            Pair.create(pair.first.getData(), pair.second.getData())
                    ));
                }
            });

            return mediator;
        }
    }

    public static class PairLiveData<A, B> extends MediatorLiveData<Pair<A, B>> {
        private A a;
        private B b;

        public PairLiveData(A a, B b) {
            super();
            this.a = a;
            this.b = b;
        }

        public void addSources(final LiveData<A> ldA, final LiveData<B> ldB,
                               final Function<B, Boolean> shouldAssignACB, final Function<A, Boolean> shouldAssignBCB
        ) {
            addSource(ldA, value -> {
                this.a = value;
                if( shouldAssignACB != null && shouldAssignACB.apply(this.b) ) {
                    setValue(new Pair<>(value, this.b));
                }
            });
            addSource(ldB, value -> {
                this.b = value;
                if( shouldAssignBCB != null && shouldAssignBCB.apply(this.a) ) {
                    setValue(new Pair<>(this.a, value));
                }
            });
        }

        public void removeSources(final LiveData<A> ldA, final LiveData<B> ldB) {
            removeSource(ldA);
            removeSource(ldB);
        }
    }
}
