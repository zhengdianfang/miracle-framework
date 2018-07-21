package com.zhengdianfang.miracleframework.permission;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

public class Permission {
    public final String name;
    public final boolean granted;
    public final boolean shouldShowRequestPermissionRationale;

    public Permission(String name, boolean granted) {
        this(name, granted, false);
    }

    public Permission(String name, boolean granted, boolean shouldShowRequestPermissionRationale) {
        this.name = name;
        this.granted = granted;
        this.shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale;
    }

    public Permission(List<Permission> permissions) {
        this.name = combineName(permissions);
        this.granted = combineGranted(permissions);
        this.shouldShowRequestPermissionRationale = combineShouldShowRequestPermissionRationale(permissions);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        final Permission that = ((Permission) obj);
        if (this.granted != that.granted) return false;
        if (this.shouldShowRequestPermissionRationale != that.shouldShowRequestPermissionRationale) return false;
        return this.name.equals(that.name);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (this.granted ? 1 : 0);
        result = 31 * result + (this.shouldShowRequestPermissionRationale ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Permission{" +
                "name='" + name + '\'' +
                ", granted=" + granted +
                ", shouldShowRequestPermissionRationale=" + shouldShowRequestPermissionRationale +
                '}';
    }

    private boolean combineShouldShowRequestPermissionRationale(List<Permission> permissions) {
        return Observable.fromIterable(permissions)
                .any(new Predicate<Permission>() {
                    @Override
                    public boolean test(Permission permission) throws Exception {
                        return permission.granted;
                    }
                }).blockingGet();
    }

    private boolean combineGranted(List<Permission> permissions) {
        return Observable.fromIterable(permissions)
                .all(new Predicate<Permission>() {
                    @Override
                    public boolean test(Permission permission) throws Exception {
                        return permission.granted;
                    }
                }).blockingGet();
    }

    private String combineName(List<Permission> permissions) {
        return Observable.fromIterable(permissions)
                .map(new Function<Permission, String>() {
                    @Override
                    public String apply(Permission permission) throws Exception {
                        return permission.name;
                    }
                }).collectInto(new StringBuffer(), new BiConsumer<StringBuffer, String>() {
            @Override
            public void accept(StringBuffer stringBuffer, String s) throws Exception {
                if (s.length() == 0) {
                    stringBuffer.append(s);
                } else {
                    stringBuffer.append(", ").append(s);
                }
            }
        }).blockingGet().delete(0, 1).toString().trim();
    }
}
