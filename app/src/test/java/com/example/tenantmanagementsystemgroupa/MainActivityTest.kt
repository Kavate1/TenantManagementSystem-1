package com.example.tenantmanagementsystemgroupa

import android.content.Intent
import android.text.InputType
import android.util.TypedValue
import androidx.databinding.DataBindingUtil
import com.example.tenantmanagementsystemgroupa.databinding.ActivityMainBinding
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MainActivityTest {
    private lateinit var controller: ActivityController<MainActivity>
    private lateinit var binding: ActivityMainBinding

    @Before
    fun setUp() {
        controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        binding = requireNotNull(
            DataBindingUtil.getBinding(controller.get().findViewById(R.id.tenantScreen))
        )
        binding.executePendingBindings()
    }

    @After
    fun tearDown() {
        controller.pause().stop().destroy()
    }

    @Test
    fun screenShowsLabelsAndPlaceholderWithoutATenant() {
        assertEquals("TENANT MANAGEMENT SYSTEM", binding.titleTextView.text.toString())
        assertEquals("Tenant Name", binding.tenantNameLabel.text.toString())
        assertEquals("Phone Number", binding.phoneLabel.text.toString())
        assertEquals("SAVE", binding.saveButton.text.toString())
        assertNull(binding.tenant)
        assertEquals("", binding.tenantResultTextView.text.toString())
        assertEquals(
            "Saved tenant will appear here",
            binding.tenantResultTextView.hint.toString()
        )
        assertEquals("", binding.savedTenantNameTextView.text.toString())
    }

    @Test
    fun inputsShowHintsAndAppropriateKeyboards() {
        assertEquals("Enter tenant name", binding.tenantNameEditText.hint.toString())
        assertEquals("Enter phone number", binding.phoneEditText.hint.toString())
        assertEquals("Enter Rent Paid", binding.rentEditText.hint.toString())
        assertEquals(InputType.TYPE_CLASS_PHONE, binding.phoneEditText.inputType)
        assertEquals(
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL,
            binding.rentEditText.inputType
        )
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun saveBindsSummaryAndBoldNameThenClearsInputs() {
        enterTenant("John Kamau", "0712345678", "25000")
        save()

        assertEquals(Tenant("John Kamau", "0712345678", "25000"), binding.tenant)
        assertEquals(
            "Tenant: John Kamau\nPhone: 0712345678\nRent paid: KSh 25000",
            binding.tenantResultTextView.text.toString()
        )
        assertEquals("John Kamau", binding.savedTenantNameTextView.text.toString())
        assertTrue(binding.savedTenantNameTextView.typeface.isBold)
        assertEquals(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                24f,
                binding.root.resources.displayMetrics
            ),
            binding.savedTenantNameTextView.textSize,
            0.1f
        )
        assertEquals("", binding.tenantNameEditText.text.toString())
        assertEquals("", binding.phoneEditText.text.toString())
        assertEquals("", binding.rentEditText.text.toString())
    }

    @Test
    fun anotherSaveUpdatesBothBoundViews() {
        enterTenant("John Kamau", "0712345678", "25000")
        save()
        enterTenant("Mary Wanjiku", "0798765432", "30000.50")
        save()

        assertEquals("Mary Wanjiku", binding.savedTenantNameTextView.text.toString())
        assertEquals(
            "Tenant: Mary Wanjiku\nPhone: 0798765432\nRent paid: KSh 30000.50",
            binding.tenantResultTextView.text.toString()
        )
    }

    @Test
    fun emptyNameShowsErrorWithoutSavingOrClearingOtherInputs() {
        enterTenant("", "0712345678", "25000")
        save()

        assertNull(binding.tenant)
        assertEquals("Tenant name is required", binding.tenantNameEditText.error.toString())
        assertTrue(binding.tenantNameEditText.hasFocus())
        assertEquals("", binding.tenantResultTextView.text.toString())
        assertEquals("0712345678", binding.phoneEditText.text.toString())
        assertEquals("25000", binding.rentEditText.text.toString())
    }

    @Test
    fun whitespaceNamePreservesPreviouslySavedTenantAndCurrentInputs() {
        enterTenant("John Kamau", "0712345678", "25000")
        save()
        enterTenant("   ", "0798765432", "30000")
        save()

        assertEquals("Tenant name is required", binding.tenantNameEditText.error.toString())
        assertEquals(Tenant("John Kamau", "0712345678", "25000"), binding.tenant)
        assertEquals("John Kamau", binding.savedTenantNameTextView.text.toString())
        assertEquals(
            "Tenant: John Kamau\nPhone: 0712345678\nRent paid: KSh 25000",
            binding.tenantResultTextView.text.toString()
        )
        assertEquals("   ", binding.tenantNameEditText.text.toString())
        assertEquals("0798765432", binding.phoneEditText.text.toString())
        assertEquals("30000", binding.rentEditText.text.toString())
    }

    @Test
    fun validFieldsClearErrorsAndTrimName() {
        save()
        enterTenant("  Mary Wanjiku  ", "0712345678", "25000")
        save()

        assertNull(binding.tenantNameEditText.error)
        assertNull(binding.phoneEditText.error)
        assertNull(binding.rentEditText.error)
        assertEquals(Tenant("Mary Wanjiku", "0712345678", "25000"), binding.tenant)
    }

    @Test
    fun blankPhoneAndRentShowErrorsWithoutSavingOrClearing() {
        enterTenant("Mary Wanjiku", "   ", "")
        save()

        assertNull(binding.tenant)
        assertNull(binding.tenantNameEditText.error)
        assertEquals("Required", binding.phoneEditText.error.toString())
        assertEquals("Required", binding.rentEditText.error.toString())
        assertTrue(binding.phoneEditText.hasFocus())
        assertEquals("Mary Wanjiku", binding.tenantNameEditText.text.toString())
    }

    @Test
    fun blankRentPreservesLastSavedTenant() {
        enterTenant("John Kamau", "0712345678", "25000")
        save()
        enterTenant("Mary Wanjiku", "0798765432", "")
        save()

        assertEquals("Required", binding.rentEditText.error.toString())
        assertTrue(binding.rentEditText.hasFocus())
        assertEquals(Tenant("John Kamau", "0712345678", "25000"), binding.tenant)
    }

    @Test
    fun callAndShareWithoutSavedTenantNotifyInsteadOfLaunching() {
        binding.callButton.performClick()
        assertEquals("Save a tenant first", ShadowToast.getTextOfLatestToast())
        assertNull(shadowOf(controller.get()).nextStartedActivity)

        binding.shareButton.performClick()
        assertEquals("Save a tenant first", ShadowToast.getTextOfLatestToast())
        assertNull(shadowOf(controller.get()).nextStartedActivity)
    }

    @Test
    fun callUsesLastSavedPhoneNotUnsavedInputs() {
        enterTenant("John Kamau", "0712345678", "25000")
        save()
        enterTenant("Mary Wanjiku", "+254798765432", "30000")
        save()
        binding.phoneEditText.setText("0111111111")
        binding.callButton.performClick()

        val intent = shadowOf(controller.get()).nextStartedActivity
        assertEquals(Intent.ACTION_DIAL, intent.action)
        assertEquals("tel", intent.data?.scheme)
        assertEquals("+254798765432", intent.data?.schemeSpecificPart)
    }

    @Test
    fun shareUsesChooserWithSavedSummaryAsPlainText() {
        enterTenant("John Kamau", "0712345678", "25000")
        save()
        binding.shareButton.performClick()

        val chooser = shadowOf(controller.get()).nextStartedActivity
        assertEquals(Intent.ACTION_CHOOSER, chooser.action)
        assertEquals("Share tenant", chooser.getStringExtra(Intent.EXTRA_TITLE))
        val send = requireNotNull(chooser.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java))
        assertEquals(Intent.ACTION_SEND, send.action)
        assertEquals("text/plain", send.type)
        assertEquals(binding.tenant?.summary(), send.getStringExtra(Intent.EXTRA_TEXT))
    }

    @Test
    fun recreationRetainsSavedTenantAndCallTarget() {
        enterTenant("John Kamau", "0712345678", "25000")
        save()
        controller.recreate()
        binding = requireNotNull(
            DataBindingUtil.getBinding(controller.get().findViewById(R.id.tenantScreen))
        )
        binding.executePendingBindings()

        assertEquals(Tenant("John Kamau", "0712345678", "25000"), binding.tenant)
        assertEquals("John Kamau", binding.savedTenantNameTextView.text.toString())
        binding.callButton.performClick()
        assertEquals("0712345678", shadowOf(controller.get()).nextStartedActivity.data?.schemeSpecificPart)
    }

    @Test
    fun assigningATenantDirectlyUpdatesBothXmlBindings() {
        binding.tenant = Tenant("Mary Wanjiku", "0712345678", "25000.50")
        binding.executePendingBindings()

        assertEquals("Mary Wanjiku", binding.savedTenantNameTextView.text.toString())
        assertEquals(
            "Tenant: Mary Wanjiku\nPhone: 0712345678\nRent paid: KSh 25000.50",
            binding.tenantResultTextView.text.toString()
        )
    }

    private fun enterTenant(name: String, phone: String, rent: String) {
        binding.tenantNameEditText.setText(name)
        binding.phoneEditText.setText(phone)
        binding.rentEditText.setText(rent)
    }

    private fun save() {
        binding.saveButton.performClick()
        binding.executePendingBindings()
    }
}
