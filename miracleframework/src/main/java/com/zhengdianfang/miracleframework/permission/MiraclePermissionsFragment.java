package com.zhengdianfang.miracleframework.permission;

import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.subjects.PublishSubject;

public class MiraclePermissionsFragment extends Fragment {
    public static final int PERMISSIONS_REQUEST_CODE = 0x000042;

    private Map<String, PublishSubject<Permission>> subjectMap = new HashMap<>();

    @TargetApi(Build.VERSION_CODES.M)
    void requestPermissions(@NonNull String[] permissions) {
        requestPermissions(permissions, PERMISSIONS_REQUEST_CODE);
        setRetainInstance(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            boolean[] shouldShowRequestPermissionRationales = new boolean[permissions.length];
            for (int i = 0; i < permissions.length; i++) {
                shouldShowRequestPermissionRationales[i] = shouldShowRequestPermissionRationale(permissions[i]);
            }
            this.onRequestPermissionsResult(permissions, grantResults, shouldShowRequestPermissionRationales);
        }
    }

    public void onRequestPermissionsResult(String[] permissions, int[] grantResults, boolean[] shouldShowRequestPermissionRationales) {
        for (int i = 0; i < permissions.length; i++) {
            PublishSubject<Permission> subject = subjectMap.get(permissions[i]);
            if (null != subject) {
                subjectMap.remove(permissions[i]);
                boolean granted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                subject.onNext(new Permission(permissions[i], granted, shouldShowRequestPermissionRationales[i]));
                subject.onComplete();
            }

        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean isGranted(String permission) {
        return getActivity().checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean isRevoked(String permission) {
        return getActivity().getPackageManager().isPermissionRevokedByPolicy(permission, getActivity().getPackageName());
    }

    public PublishSubject<Permission> getSubjectByPermission(@NonNull String permission) {
        return subjectMap.get(permission);
    }

    public boolean containsByPermission(@NonNull String permission) {
        return subjectMap.containsKey(permission);
    }

    public PublishSubject<Permission> setSubjectByPermission(@NonNull String permission, @NonNull PublishSubject<Permission> subject) {
       return subjectMap.put(permission, subject);
    }
}
