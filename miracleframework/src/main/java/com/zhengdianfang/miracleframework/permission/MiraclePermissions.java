package com.zhengdianfang.miracleframework.permission;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Function;
import io.reactivex.subjects.PublishSubject;

public class MiraclePermissions {

    static final Object TRIGGER = new Object();
    static final String TAG = "MiraclePermissions";

    public MiraclePermissionsFragment miraclePermissionsFragment;

    public MiraclePermissions(@NonNull FragmentActivity activity) {
        miraclePermissionsFragment = getMiraclePermissionsFragment(activity);
    }

    private MiraclePermissionsFragment getMiraclePermissionsFragment(FragmentActivity activity) {
        MiraclePermissionsFragment miraclePermissionsFragment = findMiraclePermissionsFragment(activity);
        boolean isNewInstance = miraclePermissionsFragment == null;
        if (isNewInstance) {
            miraclePermissionsFragment = new MiraclePermissionsFragment();
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            fragmentManager
                    .beginTransaction()
                    .add(miraclePermissionsFragment, TAG)
                    .commitNowAllowingStateLoss();
        }
        return miraclePermissionsFragment;
    }

    private MiraclePermissionsFragment findMiraclePermissionsFragment(FragmentActivity activity) {
        return (MiraclePermissionsFragment) activity.getSupportFragmentManager().findFragmentByTag(TAG);
    }

    public <T> ObservableTransformer<T,  Boolean>  ensure(final String... permissions) {
       return new ObservableTransformer<T, Boolean>() {
           @Override
           public Observable<Boolean> apply(Observable<T> upstream) {
               return request(upstream, permissions)
                       .buffer(permissions.length)
                       .flatMap(new Function<List<Permission>, ObservableSource<Boolean>>() {
                           @Override
                           public ObservableSource<Boolean> apply(List<Permission> permissions) throws Exception {
                               if (permissions.isEmpty()) {
                                   return Observable.empty();
                               }

                               for (Permission permission : permissions) {
                                   if (!permission.granted) {
                                       return Observable.just(false);
                                   }
                               }
                               return Observable.just(true);
                           }
                       });
           }
       };
    }

    public <T> ObservableTransformer<T, Permission> ensureEach(final String... permissions) {
        return new ObservableTransformer<T, Permission>() {
            @Override
            public ObservableSource<Permission> apply(Observable<T> o) {
                return request(o, permissions);
            }
        };
    }

    public <T> ObservableTransformer<T, Permission> ensureEachCombined(final String... permissions) {
        return new ObservableTransformer<T, Permission>() {
            @Override
            public ObservableSource<Permission> apply(Observable<T> o) {
                return request(o, permissions)
                        .buffer(permissions.length)
                        .flatMap(new Function<List<Permission>, ObservableSource<Permission>>() {
                            @Override
                            public ObservableSource<Permission> apply(List<Permission> permissions) throws Exception {
                                if (permissions.isEmpty()) {
                                    return Observable.empty();
                                }
                                return Observable.just(new Permission(permissions));
                            }
                        });
            }
        };
    }

    public Observable<Boolean> request(final String... permissions) {
        return Observable.just(TRIGGER).compose(ensure(permissions));
    }


    private Observable<Permission> request(final Observable<?> upstream, final String... permissions) {
        if (permissions == null || permissions.length == 0) {
            throw new IllegalArgumentException("RxPermissions.request/requestEach requires at least one input permission");
        }
        return oneOf(upstream, pending(permissions))
                .flatMap(new Function<Object, Observable<Permission>>() {
                    @Override
                    public Observable<Permission> apply(Object o) throws Exception {
                        return requestImplementation(permissions);
                    }
                });

    }


    public Observable<Boolean> shouldShowRequestPermissionRationale(final Activity activity, final String... permissions) {
        if (!isMarshmallow()) {
            return Observable.just(false);
        }
        return Observable.just(shouldShowRequestPermissionRationaleImplementation(activity, permissions));
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean shouldShowRequestPermissionRationaleImplementation(final Activity activity, final String... permissions) {
        for (String p : permissions) {
            if (!isGranted(p) && !activity.shouldShowRequestPermissionRationale(p)) {
                return false;
            }
        }
        return true;
    }

    private Observable<Permission> requestImplementation(String[] permissions) {
        ArrayList<Observable<Permission>> observables = new ArrayList<>(permissions.length);
        ArrayList<String> unrequestedPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (isGranted(permission)) {
               observables.add(Observable.just(new Permission(permission, true, false)));
               continue;
            }
            if (isRevoked(permission)) {
                observables.add(Observable.just(new Permission(permission, false, false)));
                continue;
            }
            PublishSubject<Permission> subjectByPermission = miraclePermissionsFragment.getSubjectByPermission(permission);
            if (null == subjectByPermission) {
                unrequestedPermissions.add(permission);
                subjectByPermission = PublishSubject.create();
                miraclePermissionsFragment.setSubjectByPermission(permission, subjectByPermission);
            }
            observables.add(subjectByPermission);
        }
        if (!unrequestedPermissions.isEmpty()) {
            String[] unrequestedPermissionArrays = unrequestedPermissions.toArray(new String[unrequestedPermissions.size()]);
            requestPermissionsFromFragment(unrequestedPermissionArrays);
        }
        return Observable.concat(Observable.fromIterable(observables));
    }

    public void requestPermissionsFromFragment(String[] unrequestedPermissionArrays) {
        miraclePermissionsFragment.requestPermissions(unrequestedPermissionArrays);
    }

    public boolean isRevoked(String permission) {
        return isMarshmallow() && miraclePermissionsFragment.isRevoked(permission);
    }

    public boolean isGranted(String permission) {
        return !isMarshmallow() || miraclePermissionsFragment.isGranted(permission);
    }


    public boolean isMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    private Observable<?> oneOf(Observable<?> upstream, Observable<?> pending) {
        if (upstream == null) {
            return Observable.just(TRIGGER);
        }
        return Observable.merge(upstream, pending);
    }

    private Observable<?> pending(String[] permissions) {
        for (String permission : permissions) {
            if (!miraclePermissionsFragment.containsByPermission(permission)) {
               return Observable.empty();
            }
        }
        return Observable.just(TRIGGER);
    }

    public void onRequestPermissionsResult(String[] permissions, int[] result) {
        miraclePermissionsFragment.onRequestPermissionsResult(permissions, result, new boolean[permissions.length]);
    }
}
