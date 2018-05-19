package com.zhengdianfang.miracleframework.permission;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import com.zhengdianfang.miracleframework.BuildConfig;
import com.zhengdianfang.miracleframework.TestApplication;
import com.zhengdianfang.miracleframework.fragment.SupportActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;

import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.M, application = TestApplication.class)
public class MiraclePermissionsTest {

    private SupportActivity activity;
    private MiraclePermissions miraclePermissions;

    @Before
    public void setUp() throws Exception {
        ActivityController<SupportActivity> controller = Robolectric.buildActivity(SupportActivity.class);
        activity = spy(controller.setup().get());
        miraclePermissions = spy(new MiraclePermissions(activity));
        miraclePermissions.miraclePermissionsFragment = spy(miraclePermissions.miraclePermissionsFragment);
        when(miraclePermissions.miraclePermissionsFragment.getActivity()).thenReturn(activity);
        doReturn(false).when(miraclePermissions).isGranted(anyString());
        doReturn(false).when(miraclePermissions).isRevoked(anyString());
    }

    @Test
    @TargetApi(Build.VERSION_CODES.M)
    public void subscription_perM() {
        TestObserver<Boolean> testObserver = new TestObserver<>();
        when(miraclePermissions.isGranted(Manifest.permission.CAMERA)).thenReturn(true);

        Observable.just(MiraclePermissions.TRIGGER)
                .compose(miraclePermissions.ensure(Manifest.permission.CAMERA))
                .subscribe(testObserver);
        testObserver.assertNoErrors();
        testObserver.assertTerminated();
        testObserver.assertValue(true);
    }

    @Test
    @TargetApi(Build.VERSION_CODES.M)
    public void subscription_granted() {
        TestObserver<Boolean> testObserver = new TestObserver<>();
        when(miraclePermissions.isGranted(Manifest.permission.CAMERA)).thenReturn(false);
        Observable.just(MiraclePermissions.TRIGGER)
                .compose(miraclePermissions.ensure(Manifest.permission.CAMERA))
                .subscribe(testObserver);
        miraclePermissions.onRequestPermissionsResult(new String[]{Manifest.permission.CAMERA}, new int[]{PackageManager.PERMISSION_GRANTED});
        testObserver.assertNoErrors();
        testObserver.assertTerminated();
        testObserver.assertValue(true);
    }

    @Test
    @TargetApi(Build.VERSION_CODES.M)
    public void eachSubscriptionCombined_granted() {
        TestObserver<Permission> sub = new TestObserver<>();
        String permission = Manifest.permission.READ_PHONE_STATE;
        when(miraclePermissions.isGranted(permission)).thenReturn(false);
        int[] result = new int[]{PackageManager.PERMISSION_GRANTED};

        Observable.just(MiraclePermissions.TRIGGER).compose(miraclePermissions.ensureEachCombined(permission)).subscribe(sub);
        miraclePermissions.onRequestPermissionsResult(new String[]{permission}, result);

        sub.assertNoErrors();
        sub.assertTerminated();
        sub.assertValue(new Permission(permission, true, true));
    }

    @Test
    @TargetApi(Build.VERSION_CODES.M)
    public void eachSubscription_preM() {
        TestObserver<Permission> sub = new TestObserver<>();
        String permission = Manifest.permission.READ_PHONE_STATE;
        when(miraclePermissions.isGranted(permission)).thenReturn(true);

        Observable.just(MiraclePermissions.TRIGGER).compose(miraclePermissions.ensureEach(permission)).subscribe(sub);

        sub.assertNoErrors();
        sub.assertTerminated();
        sub.assertValue(new Permission(permission, true));
    }

    @Test
    @TargetApi(Build.VERSION_CODES.M)
    public void eachSubscriptionCombined_preM() {
        TestObserver<Permission> sub = new TestObserver<>();
        String permission = Manifest.permission.READ_PHONE_STATE;
        when(miraclePermissions.isGranted(permission)).thenReturn(true);

        Observable.just(MiraclePermissions.TRIGGER).compose(miraclePermissions.ensureEachCombined(permission)).subscribe(sub);

        sub.assertNoErrors();
        sub.assertTerminated();
        sub.assertValue(new Permission(permission, true, true));
    }

    @Test
    @TargetApi(Build.VERSION_CODES.M)
    public void subscription_alreadyGranted() {
        TestObserver<Boolean> sub = new TestObserver<>();
        String permission = Manifest.permission.READ_PHONE_STATE;
        when(miraclePermissions.isGranted(permission)).thenReturn(true);

        Observable.just(MiraclePermissions.TRIGGER).compose(miraclePermissions.ensure(permission)).subscribe(sub);

        sub.assertNoErrors();
        sub.assertTerminated();
        sub.assertValue(true);
    }

    @Test
    @TargetApi(Build.VERSION_CODES.M)
    public void subscription_denied() {
        TestObserver<Boolean> sub = new TestObserver<>();
        String permission = Manifest.permission.READ_PHONE_STATE;
        when(miraclePermissions.isGranted(permission)).thenReturn(false);
        int[] result = new int[]{PackageManager.PERMISSION_DENIED};

        Observable.just(MiraclePermissions.TRIGGER).compose(miraclePermissions.ensure(permission)).subscribe(sub);
        miraclePermissions.onRequestPermissionsResult(new String[]{permission}, result);

        sub.assertNoErrors();
        sub.assertTerminated();
        sub.assertValue(false);
    }

    @Test
    @TargetApi(Build.VERSION_CODES.M)
    public void eachSubscription_denied() {
        TestObserver<Permission> sub = new TestObserver<>();
        String permission = Manifest.permission.READ_PHONE_STATE;
        when(miraclePermissions.isGranted(permission)).thenReturn(false);
        int[] result = new int[]{PackageManager.PERMISSION_DENIED};

        Observable.just(MiraclePermissions.TRIGGER).compose(miraclePermissions.ensureEach(permission)).subscribe(sub);
        miraclePermissions.onRequestPermissionsResult(new String[]{permission}, result);

        sub.assertNoErrors();
        sub.assertTerminated();
        sub.assertValue(new Permission(permission, false));
    }

    @Test
    @TargetApi(Build.VERSION_CODES.M)
    public void eachSubscriptionCombined_denied() {
        TestObserver<Permission> sub = new TestObserver<>();
        String permission = Manifest.permission.READ_PHONE_STATE;
        when(miraclePermissions.isGranted(permission)).thenReturn(false);
        int[] result = new int[]{PackageManager.PERMISSION_DENIED};

        Observable.just(MiraclePermissions.TRIGGER).compose(miraclePermissions.ensureEachCombined(permission)).subscribe(sub);
        miraclePermissions.onRequestPermissionsResult(new String[]{permission}, result);

        sub.assertNoErrors();
        sub.assertTerminated();
        sub.assertValue(new Permission(permission, false));
    }

    @Test
    @TargetApi(Build.VERSION_CODES.M)
    public void subscription_revoked() {
        TestObserver<Boolean> sub = new TestObserver<>();
        String permission = Manifest.permission.READ_PHONE_STATE;
        when(miraclePermissions.isRevoked(permission)).thenReturn(true);

        Observable.just(MiraclePermissions.TRIGGER).compose(miraclePermissions.ensure(permission)).subscribe(sub);

        sub.assertNoErrors();
        sub.assertTerminated();
        sub.assertValue(false);
    }

    @Test
    @TargetApi(Build.VERSION_CODES.M)
    public void eachSubscription_revoked() {
        TestObserver<Permission> sub = new TestObserver<>();
        String permission = Manifest.permission.READ_PHONE_STATE;
        when(miraclePermissions.isRevoked(permission)).thenReturn(true);

        Observable.just(MiraclePermissions.TRIGGER).compose(miraclePermissions.ensureEach(permission)).subscribe(sub);

        sub.assertNoErrors();
        sub.assertTerminated();
        sub.assertValue(new Permission(permission, false));
    }

    @Test
    @TargetApi(Build.VERSION_CODES.M)
    public void eachSubscriptionCombined_revoked() {
        TestObserver<Permission> sub = new TestObserver<>();
        String permission = Manifest.permission.READ_PHONE_STATE;
        when(miraclePermissions.isRevoked(permission)).thenReturn(true);

        Observable.just(MiraclePermissions.TRIGGER).compose(miraclePermissions.ensureEachCombined(permission)).subscribe(sub);

        sub.assertNoErrors();
        sub.assertTerminated();
        sub.assertValue(new Permission(permission, false));
    }

    @Test
    @TargetApi(Build.VERSION_CODES.M)
    public void subscription_severalPermissions_granted() {
        TestObserver<Boolean> sub = new TestObserver<>();
        String[] permissions = new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.CAMERA};
        when(miraclePermissions.isGranted("")).thenReturn(false);
        int[] result = new int[]{PackageManager.PERMISSION_GRANTED, PackageManager.PERMISSION_GRANTED};

        Observable.just(MiraclePermissions.TRIGGER).compose(miraclePermissions.ensure(permissions)).subscribe(sub);
        miraclePermissions.onRequestPermissionsResult(permissions, result);

        sub.assertNoErrors();
        sub.assertTerminated();
        sub.assertValue(true);
    }

    @Test
    @TargetApi(Build.VERSION_CODES.M)
    public void eachSubscription_severalPermissions_granted() {
        TestObserver<Permission> sub = new TestObserver<>();
        String[] permissions = new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.CAMERA};
        when(miraclePermissions.isGranted("")).thenReturn(false);
        int[] result = new int[]{PackageManager.PERMISSION_GRANTED, PackageManager.PERMISSION_GRANTED};

        Observable.just(MiraclePermissions.TRIGGER).compose(miraclePermissions.ensureEach(permissions)).subscribe(sub);
        miraclePermissions.onRequestPermissionsResult(permissions, result);

        sub.assertNoErrors();
        sub.assertTerminated();
        sub.assertValues(new Permission(permissions[0], true), new Permission(permissions[1], true));
    }

    @Test
    @TargetApi(Build.VERSION_CODES.M)
    public void eachSubscriptionCombined_severalPermissions_granted() {
        TestObserver<Permission> sub = new TestObserver<>();
        String[] permissions = new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.CAMERA};
        when(miraclePermissions.isGranted("")).thenReturn(false);
        int[] result = new int[]{PackageManager.PERMISSION_GRANTED, PackageManager.PERMISSION_GRANTED};

        Observable.just(MiraclePermissions.TRIGGER).compose(miraclePermissions.ensureEachCombined(permissions)).subscribe(sub);
        miraclePermissions.onRequestPermissionsResult(permissions, result);

        sub.assertNoErrors();
        sub.assertTerminated();
        sub.assertValues(new Permission(permissions[0] + ", " + permissions[1], true, true));
    }

    @Test
    @TargetApi(Build.VERSION_CODES.M)
    public void subscription_severalPermissions_oneDenied() {
        TestObserver<Boolean> sub = new TestObserver<>();
        String[] permissions = new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.CAMERA};
        when(miraclePermissions.isGranted("")).thenReturn(false);
        int[] result = new int[]{PackageManager.PERMISSION_GRANTED, PackageManager.PERMISSION_DENIED};

        Observable.just(MiraclePermissions.TRIGGER).compose(miraclePermissions.ensure(permissions)).subscribe(sub);
        miraclePermissions.onRequestPermissionsResult(permissions, result);

        sub.assertNoErrors();
        sub.assertTerminated();
        sub.assertValue(false);
    }

    @Test
    @TargetApi(Build.VERSION_CODES.M)
    public void subscription_severalPermissions_oneRevoked() {
        TestObserver<Boolean> sub = new TestObserver<>();
        String[] permissions = new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.CAMERA};
        when(miraclePermissions.isGranted("")).thenReturn(false);
        when(miraclePermissions.isRevoked(Manifest.permission.CAMERA)).thenReturn(true);

        Observable.just(MiraclePermissions.TRIGGER).compose(miraclePermissions.ensure(permissions)).subscribe(sub);
        miraclePermissions.onRequestPermissionsResult(
                new String[]{Manifest.permission.READ_PHONE_STATE},
                new int[]{PackageManager.PERMISSION_GRANTED});

        sub.assertNoErrors();
        sub.assertTerminated();
        sub.assertValue(false);
    }

    @Test
    @TargetApi(Build.VERSION_CODES.M)
    public void eachSubscription_severalPermissions_oneAlreadyGranted() {
        TestObserver<Permission> sub = new TestObserver<>();
        String[] permissions = new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.CAMERA};
        when(miraclePermissions.isGranted("")).thenReturn(false);
        when(miraclePermissions.isGranted(Manifest.permission.CAMERA)).thenReturn(true);

        Observable.just(MiraclePermissions.TRIGGER).compose(miraclePermissions.ensureEach(permissions)).subscribe(sub);
        miraclePermissions.onRequestPermissionsResult(
                new String[]{Manifest.permission.READ_PHONE_STATE},
                new int[]{PackageManager.PERMISSION_GRANTED});

        sub.assertNoErrors();
        sub.assertTerminated();
        sub.assertValues(new Permission(permissions[0], true), new Permission(permissions[1], true));
        ArgumentCaptor<String[]> requestedPermissions = ArgumentCaptor.forClass(String[].class);
        verify(miraclePermissions).requestPermissionsFromFragment(requestedPermissions.capture());
        assertEquals(1, requestedPermissions.getValue().length);
        assertEquals(Manifest.permission.READ_PHONE_STATE, requestedPermissions.getValue()[0]);
    }

    @Test
    @TargetApi(Build.VERSION_CODES.M)
    public void eachSubscriptionCombined_severalPermissions_oneAlreadyGranted() {
        TestObserver<Permission> sub = new TestObserver<>();
        String[] permissions = new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.CAMERA};
        when(miraclePermissions.isGranted("")).thenReturn(false);
        when(miraclePermissions.isGranted(Manifest.permission.CAMERA)).thenReturn(true);

        Observable.just(MiraclePermissions.TRIGGER).compose(miraclePermissions.ensureEachCombined(permissions)).subscribe(sub);
        miraclePermissions.onRequestPermissionsResult(
                new String[]{Manifest.permission.READ_PHONE_STATE},
                new int[]{PackageManager.PERMISSION_GRANTED});

        sub.assertNoErrors();
        sub.assertTerminated();
        sub.assertValues(new Permission(permissions[0] + ", " + permissions[1], true, true));
        ArgumentCaptor<String[]> requestedPermissions = ArgumentCaptor.forClass(String[].class);
        verify(miraclePermissions).requestPermissionsFromFragment(requestedPermissions.capture());
        assertEquals(1, requestedPermissions.getValue().length);
        assertEquals(Manifest.permission.READ_PHONE_STATE, requestedPermissions.getValue()[0]);
    }

    @Test
    @TargetApi(Build.VERSION_CODES.M)
    public void eachSubscription_severalPermissions_oneDenied() {
        TestObserver<Permission> sub = new TestObserver<>();
        String[] permissions = new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.CAMERA};
        when(miraclePermissions.isGranted("")).thenReturn(false);
        int[] result = new int[]{PackageManager.PERMISSION_GRANTED, PackageManager.PERMISSION_DENIED};

        Observable.just(MiraclePermissions.TRIGGER).compose(miraclePermissions.ensureEach(permissions)).subscribe(sub);
        miraclePermissions.onRequestPermissionsResult(permissions, result);

        sub.assertNoErrors();
        sub.assertTerminated();
        sub.assertValues(new Permission(permissions[0], true), new Permission(permissions[1], false));
    }

    @Test
    @TargetApi(Build.VERSION_CODES.M)
    public void eachSubscriptionCombined_severalPermissions_oneDenied() {
        TestObserver<Permission> sub = new TestObserver<>();
        String[] permissions = new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.CAMERA};
        when(miraclePermissions.isGranted("")).thenReturn(false);
        int[] result = new int[]{PackageManager.PERMISSION_GRANTED, PackageManager.PERMISSION_DENIED};

        Observable.just(MiraclePermissions.TRIGGER).compose(miraclePermissions.ensureEachCombined(permissions)).subscribe(sub);
        miraclePermissions.onRequestPermissionsResult(permissions, result);

        sub.assertNoErrors();
        sub.assertTerminated();
        sub.assertValues(new Permission(permissions[0] + ", " + permissions[1], false, true));
    }

    @Test
    @TargetApi(Build.VERSION_CODES.M)
    public void eachSubscription_severalPermissions_oneRevoked() {
        TestObserver<Permission> sub = new TestObserver<>();
        String[] permissions = new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.CAMERA};
        when(miraclePermissions.isGranted("")).thenReturn(false);
        when(miraclePermissions.isRevoked(Manifest.permission.CAMERA)).thenReturn(true);

        Observable.just(MiraclePermissions.TRIGGER).compose(miraclePermissions.ensureEach(permissions)).subscribe(sub);
        miraclePermissions.onRequestPermissionsResult(
                new String[]{Manifest.permission.READ_PHONE_STATE},
                new int[]{PackageManager.PERMISSION_GRANTED});

        sub.assertNoErrors();
        sub.assertTerminated();
        sub.assertValues(new Permission(permissions[0], true), new Permission(permissions[1], false));
    }

    @Test
    @TargetApi(Build.VERSION_CODES.M)
    public void eachSubscriptionCombined_severalPermissions_oneRevoked() {
        TestObserver<Permission> sub = new TestObserver<>();
        String[] permissions = new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.CAMERA};
        when(miraclePermissions.isGranted("")).thenReturn(false);
        when(miraclePermissions.isRevoked(Manifest.permission.CAMERA)).thenReturn(true);

        Observable.just(MiraclePermissions.TRIGGER).compose(miraclePermissions.ensureEachCombined(permissions)).subscribe(sub);
        miraclePermissions.onRequestPermissionsResult(
                new String[]{Manifest.permission.READ_PHONE_STATE},
                new int[]{PackageManager.PERMISSION_GRANTED});

        sub.assertNoErrors();
        sub.assertTerminated();
        sub.assertValues(new Permission( permissions[0] + ", " + permissions[1], false, true));
    }

    @Test
    @TargetApi(Build.VERSION_CODES.M)
    public void subscription_trigger_granted() {
        TestObserver<Boolean> sub = new TestObserver<>();
        String permission = Manifest.permission.READ_PHONE_STATE;
        when(miraclePermissions.isGranted(permission)).thenReturn(false);
        int[] result = new int[]{PackageManager.PERMISSION_GRANTED};
        PublishSubject<Object> trigger = PublishSubject.create();

        trigger.compose(miraclePermissions.ensure(permission)).subscribe(sub);
        trigger.onNext(1);
        miraclePermissions.onRequestPermissionsResult(new String[]{permission}, result);

        sub.assertNoErrors();
        sub.assertNotTerminated();
        sub.assertValue(true);
    }

    @Test
    @TargetApi(Build.VERSION_CODES.M)
    public void eachSubscription_trigger_granted() {
        TestObserver<Permission> sub = new TestObserver<>();
        String permission = Manifest.permission.READ_PHONE_STATE;
        when(miraclePermissions.isGranted(permission)).thenReturn(false);
        int[] result = new int[]{PackageManager.PERMISSION_GRANTED};
        PublishSubject<Object> trigger = PublishSubject.create();

        trigger.compose(miraclePermissions.ensureEach(permission)).subscribe(sub);
        trigger.onNext(1);
        miraclePermissions.onRequestPermissionsResult(new String[]{permission}, result);

        sub.assertNoErrors();
        sub.assertNotTerminated();
        sub.assertValue(new Permission(permission, true));
    }

    @Test
    @TargetApi(Build.VERSION_CODES.M)
    public void eachSubscriptionCombined_trigger_granted() {
        TestObserver<Permission> sub = new TestObserver<>();
        String permission = Manifest.permission.READ_PHONE_STATE;
        when(miraclePermissions.isGranted(permission)).thenReturn(false);
        int[] result = new int[]{PackageManager.PERMISSION_GRANTED};
        PublishSubject<Object> trigger = PublishSubject.create();

        trigger.compose(miraclePermissions.ensureEachCombined(permission)).subscribe(sub);
        trigger.onNext(1);
        miraclePermissions.onRequestPermissionsResult(new String[]{permission}, result);

        sub.assertNoErrors();
        sub.assertNotTerminated();
        sub.assertValue(new Permission(permission, true, true));
    }

    @Test
    @TargetApi(Build.VERSION_CODES.M)
    public void shouldShowRequestPermissionRationale_allDenied_allRationale() {
        when(miraclePermissions.isMarshmallow()).thenReturn(true);
        Activity activity = mock(Activity.class);
        when(activity.shouldShowRequestPermissionRationale(anyString())).thenReturn(true);

        TestObserver<Boolean> sub = new TestObserver<>();
        miraclePermissions.shouldShowRequestPermissionRationale(activity, new String[]{"p1", "p2"})
                .subscribe(sub);

        sub.assertComplete();
        sub.assertNoErrors();
        sub.assertValue(true);
    }

    @Test
    @TargetApi(Build.VERSION_CODES.M)
    public void shouldShowRequestPermissionRationale_allDenied_oneRationale() {
        when(miraclePermissions.isMarshmallow()).thenReturn(true);
        Activity activity = mock(Activity.class);
        when(activity.shouldShowRequestPermissionRationale("p1")).thenReturn(true);

        TestObserver<Boolean> sub = new TestObserver<>();
        miraclePermissions.shouldShowRequestPermissionRationale(activity, new String[]{"p1", "p2"})
                .subscribe(sub);

        sub.assertComplete();
        sub.assertNoErrors();
        sub.assertValue(false);
    }

    @Test
    @TargetApi(Build.VERSION_CODES.M)
    public void shouldShowRequestPermissionRationale_allDenied_noRationale() {
        when(miraclePermissions.isMarshmallow()).thenReturn(true);
        Activity activity = mock(Activity.class);

        TestObserver<Boolean> sub = new TestObserver<>();
        miraclePermissions.shouldShowRequestPermissionRationale(activity, new String[]{"p1", "p2"})
                .subscribe(sub);

        sub.assertComplete();
        sub.assertNoErrors();
        sub.assertValue(false);
    }

    @Test
    @TargetApi(Build.VERSION_CODES.M)
    public void shouldShowRequestPermissionRationale_oneDeniedRationale() {
        when(miraclePermissions.isMarshmallow()).thenReturn(true);
        Activity activity = mock(Activity.class);
        when(miraclePermissions.isGranted("p1")).thenReturn(true);
        when(activity.shouldShowRequestPermissionRationale("p2")).thenReturn(true);

        TestObserver<Boolean> sub = new TestObserver<>();
        miraclePermissions.shouldShowRequestPermissionRationale(activity, new String[]{"p1", "p2"})
                .subscribe(sub);

        sub.assertComplete();
        sub.assertNoErrors();
        sub.assertValue(true);
    }

    @Test
    @TargetApi(Build.VERSION_CODES.M)
    public void shouldShowRequestPermissionRationale_oneDeniedNotRationale() {
        when(miraclePermissions.isMarshmallow()).thenReturn(true);
        Activity activity = mock(Activity.class);
        when(miraclePermissions.isGranted("p2")).thenReturn(true);

        TestObserver<Boolean> sub = new TestObserver<>();
        miraclePermissions.shouldShowRequestPermissionRationale(activity, new String[]{"p1", "p2"})
                .subscribe(sub);

        sub.assertComplete();
        sub.assertNoErrors();
        sub.assertValue(false);
    }

    @Test
    public void isGranted_preMarshmallow() {
        // unmock isGranted
        doCallRealMethod().when(miraclePermissions).isGranted(anyString());
        doReturn(false).when(miraclePermissions).isMarshmallow();

        boolean granted = miraclePermissions.isGranted("p");

        assertTrue(granted);
    }

    @Test
    @TargetApi(Build.VERSION_CODES.M)
    public void isGranted_granted() {
        // unmock isGranted
        doCallRealMethod().when(miraclePermissions).isGranted(anyString());
        doReturn(true).when(miraclePermissions).isMarshmallow();
        when(activity.checkSelfPermission("p")).thenReturn(PackageManager.PERMISSION_GRANTED);

        boolean granted = miraclePermissions.isGranted("p");

        assertTrue(granted);
    }

    @Test
    @TargetApi(Build.VERSION_CODES.M)
    public void isGranted_denied() {
        // unmock isGranted
        doCallRealMethod().when(miraclePermissions).isGranted(anyString());
        doReturn(true).when(miraclePermissions).isMarshmallow();
        when(activity.checkSelfPermission("p")).thenReturn(PackageManager.PERMISSION_DENIED);

        boolean granted = miraclePermissions.isGranted("p");

        assertFalse(granted);
    }

    @Test
    public void isRevoked_preMarshmallow() {
        // unmock isRevoked
        doCallRealMethod().when(miraclePermissions).isRevoked(anyString());
        doReturn(false).when(miraclePermissions).isMarshmallow();

        boolean revoked = miraclePermissions.isRevoked("p");

        assertFalse(revoked);
    }

    @Test
    @TargetApi(Build.VERSION_CODES.M)
    public void isRevoked_true() {
        // unmock isRevoked
        doCallRealMethod().when(miraclePermissions).isRevoked(anyString());
        doReturn(true).when(miraclePermissions).isMarshmallow();
        PackageManager pm = mock(PackageManager.class);
        when(activity.getPackageManager()).thenReturn(pm);
        when(pm.isPermissionRevokedByPolicy(eq("p"), anyString())).thenReturn(true);

        boolean revoked = miraclePermissions.isRevoked("p");

        assertTrue(revoked);
    }

    @Test
    @TargetApi(Build.VERSION_CODES.M)
    public void isGranted_false() {
        // unmock isRevoked
        doCallRealMethod().when(miraclePermissions).isRevoked(anyString());
        doReturn(true).when(miraclePermissions).isMarshmallow();
        PackageManager pm = mock(PackageManager.class);
        when(activity.getPackageManager()).thenReturn(pm);
        when(pm.isPermissionRevokedByPolicy(eq("p"), anyString())).thenReturn(false);

        boolean revoked = miraclePermissions.isRevoked("p");

        assertFalse(revoked);
    }
}