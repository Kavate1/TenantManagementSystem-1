package com.example.tenantmanagementsystemgroupa

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.example.tenantmanagementsystemgroupa.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemInsets(binding.root)

        if (savedInstanceState == null) {
            intent.getStringExtra(EXTRA_EMAIL)?.let(binding.emailEditText::setText)
        }

        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString()
            if (email.isEmpty() || password.isBlank()) {
                Toast.makeText(this, R.string.login_fields_required, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            startActivity(Intent(this, MainActivity::class.java).putExtra(EXTRA_EMAIL, email))
            finish()
        }

        binding.registerTextView.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.helpTextView.setOnClickListener {
            openExternalIntent(
                Intent(Intent.ACTION_VIEW, "https://www.strathmore.edu".toUri()),
                R.string.browser_unavailable
            )
        }
    }

    companion object {
        const val EXTRA_EMAIL = "EMAIL"
    }
}
