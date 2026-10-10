package com.example.tenantmanagementsystemgroupa

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tenantmanagementsystemgroupa.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applySystemInsets(binding.root)

        savedInstanceState?.getString(STATE_TENANT_NAME)?.let { name ->
            binding.tenant = Tenant(
                name,
                requireNotNull(savedInstanceState.getString(STATE_TENANT_PHONE)),
                requireNotNull(savedInstanceState.getString(STATE_TENANT_RENT))
            )
        }
        if (savedInstanceState == null) {
            intent.getStringExtra(LoginActivity.EXTRA_EMAIL)?.let { email ->
                Toast.makeText(this, getString(R.string.logged_in_as, email), Toast.LENGTH_SHORT).show()
            }
        }

        binding.saveButton.setOnClickListener {
            val name = binding.tenantNameEditText.text.toString().trim()
            val phone = binding.phoneEditText.text.toString().trim()
            val rent = binding.rentEditText.text.toString().trim()
            binding.tenantNameEditText.error =
                if (name.isEmpty()) getString(R.string.tenant_name_required) else null
            binding.phoneEditText.error =
                if (phone.isEmpty()) getString(R.string.required) else null
            binding.rentEditText.error =
                if (rent.isEmpty()) getString(R.string.required) else null
            val invalidField = listOf(
                binding.tenantNameEditText, binding.phoneEditText, binding.rentEditText
            ).firstOrNull { it.error != null }
            if (invalidField != null) {
                invalidField.requestFocus()
                return@setOnClickListener
            }

            binding.tenant = Tenant(name, phone, rent)

            binding.tenantNameEditText.text.clear()
            binding.phoneEditText.text.clear()
            binding.rentEditText.text.clear()
        }

        binding.callButton.setOnClickListener {
            val tenant = savedTenantOrNotify() ?: return@setOnClickListener
            openExternalIntent(
                Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", tenant.phone, null)),
                R.string.dialer_unavailable
            )
        }

        binding.shareButton.setOnClickListener {
            val tenant = savedTenantOrNotify() ?: return@setOnClickListener
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, tenant.summary())
            }
            openExternalIntent(
                Intent.createChooser(intent, getString(R.string.share_tenant)),
                R.string.sharing_unavailable
            )
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        binding.tenant?.let { tenant ->
            outState.putString(STATE_TENANT_NAME, tenant.name)
            outState.putString(STATE_TENANT_PHONE, tenant.phone)
            outState.putString(STATE_TENANT_RENT, tenant.rent)
        }
        super.onSaveInstanceState(outState)
    }

    private fun savedTenantOrNotify(): Tenant? {
        val tenant = binding.tenant
        if (tenant == null) {
            Toast.makeText(this, R.string.save_tenant_first, Toast.LENGTH_SHORT).show()
        }
        return tenant
    }

    private companion object {
        const val STATE_TENANT_NAME = "tenant.name"
        const val STATE_TENANT_PHONE = "tenant.phone"
        const val STATE_TENANT_RENT = "tenant.rent"
    }
}
