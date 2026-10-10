package com.example.tenantmanagementsystemgroupa

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tenantmanagementsystemgroupa.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemInsets(binding.root)

        binding.registerButton.setOnClickListener {
            val fullName = binding.fullNameEditText.text.toString().trim()
            val email = binding.registerEmailEditText.text.toString().trim()
            val password = binding.registerPasswordEditText.text.toString()
            if (fullName.isEmpty() || email.isEmpty() || password.isBlank()) {
                Toast.makeText(this, R.string.register_fields_required, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, R.string.account_created, Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                putExtra(LoginActivity.EXTRA_EMAIL, email)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            })
            finish()
        }

        binding.loginLinkTextView.setOnClickListener {
            finish()
        }
    }
}
