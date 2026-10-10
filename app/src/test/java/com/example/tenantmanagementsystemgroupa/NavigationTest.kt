package com.example.tenantmanagementsystemgroupa

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.text.InputType
import androidx.appcompat.app.AppCompatActivity
import com.example.tenantmanagementsystemgroupa.databinding.ActivityLoginBinding
import com.example.tenantmanagementsystemgroupa.databinding.ActivityRegisterBinding
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class NavigationTest {
    @Test
    fun loginIsTheOnlyLauncherAndInternalScreensAreNotExported() {
        val context = RuntimeEnvironment.getApplication()
        val launcher = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            setPackage(context.packageName)
        }
        val activities = context.packageManager.queryIntentActivities(launcher, 0)
        assertEquals(1, activities.size)
        assertEquals(LoginActivity::class.java.name, activities.single().activityInfo.name)
        assertTrue(activities.single().activityInfo.exported)
        val info = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_ACTIVITIES)
        for (activity in requireNotNull(info.activities)) {
            if (activity.name != LoginActivity::class.java.name) {
                assertFalse(activity.exported)
            }
        }
        val permissions = context.packageManager.getPackageInfo(
            context.packageName, PackageManager.GET_PERMISSIONS
        ).requestedPermissions.orEmpty()
        assertFalse(permissions.contains("android.permission.CALL_PHONE"))
    }

    @Test
    fun loginRejectsEachMissingFieldAndWhitespace() {
        Robolectric.buildActivity(LoginActivity::class.java).setup().use { controller ->
            val activity = controller.get()
            val binding = ActivityLoginBinding.bind(activity.findViewById(R.id.loginScreen))
            assertEquals(
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD,
                binding.passwordEditText.inputType
            )
            for ((email, password) in listOf("" to "", "" to "secret", "a@b.com" to "", " " to "secret", "a@b.com" to " ")) {
                binding.emailEditText.setText(email)
                binding.passwordEditText.setText(password)
                binding.loginButton.performClick()
                assertNull(shadowOf(activity).nextStartedActivity)
                assertFalse(activity.isFinishing)
                assertEquals("Please enter your email and password", ShadowToast.getTextOfLatestToast())
            }
        }
    }

    @Test
    fun loginPassesEmailWelcomesUserAndFinishes() {
        Robolectric.buildActivity(LoginActivity::class.java).setup().use { controller ->
            val activity = controller.get()
            val binding = ActivityLoginBinding.bind(activity.findViewById(R.id.loginScreen))
            binding.emailEditText.setText("  mary@example.com  ")
            binding.passwordEditText.setText("secret")
            binding.loginButton.performClick()

            val intent = shadowOf(activity).nextStartedActivity
            assertEquals(MainActivity::class.java.name, intent.component?.className)
            assertEquals("mary@example.com", intent.getStringExtra(LoginActivity.EXTRA_EMAIL))
            assertFalse(intent.hasExtra("PASSWORD"))
            assertTrue(activity.isFinishing)
            Robolectric.buildActivity(MainActivity::class.java, intent).setup().use { main ->
                assertEquals("Logged in as mary@example.com", ShadowToast.getTextOfLatestToast())
                ShadowToast.reset()
                main.recreate()
                assertNull(ShadowToast.getTextOfLatestToast())
            }
        }
    }

    @Test
    fun registerLinkOpensRegisterWithoutClosingLogin() {
        Robolectric.buildActivity(LoginActivity::class.java).setup().use { controller ->
            val activity = controller.get()
            ActivityLoginBinding.bind(activity.findViewById(R.id.loginScreen))
                .registerTextView.performClick()
            assertEquals(
                RegisterActivity::class.java.name,
                shadowOf(activity).nextStartedActivity.component?.className
            )
            assertFalse(activity.isFinishing)
        }
    }

    @Test
    fun registerRequiresAllFields() {
        Robolectric.buildActivity(RegisterActivity::class.java).setup().use { controller ->
            val activity = controller.get()
            val binding = ActivityRegisterBinding.bind(activity.findViewById(R.id.registerScreen))
            for ((name, email, password) in listOf(
                Triple("", "mary@example.com", "secret"),
                Triple("Mary", "", "secret"),
                Triple("Mary", "mary@example.com", ""),
                Triple("   ", "mary@example.com", "secret"),
                Triple("Mary", "   ", "secret"),
                Triple("Mary", "mary@example.com", "   ")
            )) {
                binding.fullNameEditText.setText(name)
                binding.registerEmailEditText.setText(email)
                binding.registerPasswordEditText.setText(password)
                binding.registerButton.performClick()
                assertNull(shadowOf(activity).nextStartedActivity)
                assertFalse(activity.isFinishing)
                assertEquals("Please fill in all fields", ShadowToast.getTextOfLatestToast())
            }
        }
    }

    @Test
    fun registrationClearsBackStackAndPrefillsLoginEmail() {
        Robolectric.buildActivity(RegisterActivity::class.java).setup().use { controller ->
            val activity = controller.get()
            val binding = ActivityRegisterBinding.bind(activity.findViewById(R.id.registerScreen))
            binding.fullNameEditText.setText("Mary Wanjiku")
            binding.registerEmailEditText.setText("  mary@example.com  ")
            binding.registerPasswordEditText.setText("secret")
            binding.registerButton.performClick()

            val intent = shadowOf(activity).nextStartedActivity
            assertEquals(LoginActivity::class.java.name, intent.component?.className)
            assertEquals(Intent.FLAG_ACTIVITY_CLEAR_TOP, intent.flags)
            assertEquals("mary@example.com", intent.getStringExtra(LoginActivity.EXTRA_EMAIL))
            assertEquals(setOf(LoginActivity.EXTRA_EMAIL), intent.extras?.keySet())
            assertEquals("Account created. Please log in.", ShadowToast.getTextOfLatestToast())
            assertTrue(activity.isFinishing)
            Robolectric.buildActivity(LoginActivity::class.java, intent).setup().use { login ->
                val loginBinding = ActivityLoginBinding.bind(login.get().findViewById(R.id.loginScreen))
                assertEquals("mary@example.com", loginBinding.emailEditText.text.toString())
                assertEquals("", loginBinding.passwordEditText.text.toString())
                loginBinding.emailEditText.setText("edited@example.com")
                login.recreate()
                val recreated = ActivityLoginBinding.bind(login.get().findViewById(R.id.loginScreen))
                assertEquals("edited@example.com", recreated.emailEditText.text.toString())
            }
        }
    }

    @Test
    fun loginLinkClosesRegisterWithoutLaunchingDuplicateLogin() {
        Robolectric.buildActivity(RegisterActivity::class.java).setup().use { controller ->
            val activity = controller.get()
            ActivityRegisterBinding.bind(activity.findViewById(R.id.registerScreen))
                .loginLinkTextView.performClick()
            assertTrue(activity.isFinishing)
            assertNull(shadowOf(activity).nextStartedActivity)
        }
    }

    @Test
    fun helpOpensStrathmoreWebsiteWithImplicitIntent() {
        Robolectric.buildActivity(LoginActivity::class.java).setup().use { controller ->
            val activity = controller.get()
            ActivityLoginBinding.bind(activity.findViewById(R.id.loginScreen))
                .helpTextView.performClick()
            val intent = shadowOf(activity).nextStartedActivity
            assertEquals(Intent.ACTION_VIEW, intent.action)
            assertEquals("https://www.strathmore.edu", intent.dataString)
            assertNull(intent.component)
            assertFalse(activity.isFinishing)
        }
    }

    @Test
    fun missingExternalAppsShowExplicitMessages() {
        Robolectric.buildActivity(NoExternalAppsActivity::class.java).setup().use { controller ->
            val activity = controller.get()
            for ((intent, message) in listOf(
                Intent(Intent.ACTION_VIEW, Uri.parse("https://www.strathmore.edu")) to R.string.browser_unavailable,
                Intent(Intent.ACTION_DIAL, Uri.parse("tel:0712345678")) to R.string.dialer_unavailable,
                Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain"), "Share") to R.string.sharing_unavailable
            )) {
                activity.openExternalIntent(intent, message)
                assertEquals(activity.getString(message), ShadowToast.getTextOfLatestToast())
            }
        }
    }

    class NoExternalAppsActivity : AppCompatActivity() {
        override fun startActivity(intent: Intent) {
            throw ActivityNotFoundException("No handler installed")
        }
    }
}
