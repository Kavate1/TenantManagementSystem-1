package com.example.tenantmanagementsystemgroupa

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
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

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
    fun validNameClearsErrorAndAllowsOptionalPhoneAndRent() {
        save()
        enterTenant("  Mary Wanjiku  ", "", "")
        save()

        assertNull(binding.tenantNameEditText.error)
        assertEquals(Tenant("Mary Wanjiku", "", ""), binding.tenant)
        assertEquals(
            "Tenant: Mary Wanjiku\nPhone: \nRent paid: KSh ",
            binding.tenantResultTextView.text.toString()
        )
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
