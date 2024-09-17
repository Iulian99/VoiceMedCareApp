package com.example.voicemedcareapp

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.voicemedcareapp.Profile.UserActivity
import com.example.voicemedcareapp.medicalReports.SharedViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.play.core.integrity.IntegrityManager
import com.google.firebase.appcheck.FirebaseAppCheck

class
MainActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var navController: NavController
    private var btnLeft: ImageButton? = null
    private var btnRight: ImageButton? = null
    private var navView: BottomNavigationView? = null
    private lateinit var integrityManager: IntegrityManager
    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupViews()

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            when (destination.id) {
                R.id.fragment_account -> toolbar.visibility = View.GONE
                else -> toolbar.visibility = View.VISIBLE
            }
        }
        val role = intent.getStringExtra("role") ?: "doctor"
        val userId = intent.getStringExtra("userId") ?: ""

        sharedViewModel.setRole(role)
        sharedViewModel.setUserId(userId)

        setupToolbar()


        setupNavigation()


    }
    override fun onDestroy() {
        super.onDestroy()
    }
    private fun fetchAppCheckTokenWithRetry(retryCount: Int = 3) {
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.getToken(false).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Token obținut cu succes
                val token = task.result?.token
                Log.d("AppCheck", "Token received successfully: $token")
            } else {
                // Eroare la obținerea token-ului
                if (retryCount > 0) {
                    // Reîncercare cu backoff
                    Handler(Looper.getMainLooper()).postDelayed({
                        fetchAppCheckTokenWithRetry(retryCount - 1)
                    }, 2000) // 2 secunde delay
                } else {
                    Log.e("AppCheck", "Failed to obtain App Check token after retries")
                }
            }
        }
    }

    private fun setupViews() {
        toolbar = findViewById(R.id.toolbar2)
        btnLeft = findViewById(R.id.notifications_image_button)
        btnRight = findViewById(R.id.account_image_button)
        navView = findViewById(R.id.bottom_navigation)
        navController = findNavController(R.id.nav_host_fragment)
    }

    private fun setupToolbar() {
        toolbar.let { tb ->
            setSupportActionBar(tb)
            supportActionBar?.setDisplayShowTitleEnabled(false)
            Log.d("MainActivity", "Toolbar is set")
        }
        toolbar.visibility = View.GONE
    }

    private fun setupNavigation() {
        val role = intent.getStringExtra("role") ?: "doctor" // Preia rolul transmis de LoginActivity
        val userId = intent.getStringExtra("userId") // Preia userId transmis de LoginActivity

        navView?.let { navViewNonNull ->
            when (role) {
                "doctor" -> userId?.let {
                    setBottomNavigationDoctor(navViewNonNull, navController,
                        it
                    )
                }
                "patient" -> userId?.let {
                    setBottomNavigationPatient(navViewNonNull, navController,
                        it
                    )
                }
                else -> Log.e("MainActivity", "Invalid role specified")
            }
        } ?: Log.e("MainActivity", "Navigation view not initialized")

        btnRight?.setOnClickListener {
            val intent = Intent(this, UserActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(this, R.anim.slide_in_right, R.anim.slide_out_left)
            startActivity(intent, options.toBundle())
        }

        btnLeft?.setOnClickListener {
            toolbar.visibility = View.GONE
            navController.navigate(R.id.fragment_account)
        }
    }

    private fun setBottomNavigationDoctor(navView: BottomNavigationView, navController: NavController, userId: String) {
        navView.menu.clear()
        navView.inflateMenu(R.menu.bottom_nav_menu_doctor)
        Toast.makeText(this, "Doctor Menu Loaded", Toast.LENGTH_SHORT).show()

        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_patient_management -> {
                    val bundle = Bundle()
                    bundle.putString("doctorId", userId) // Transmite doctorId către fragment
                    navController.navigate(R.id.fragment_notification, bundle)
                    Toast.makeText(this, "Management Doctor", Toast.LENGTH_SHORT).show()
                }
                R.id.navigation_medical_reports -> {
                    val bundle = Bundle()
                    bundle.putString("doctorId", userId) // Transmite doctorId către fragment
                    navController.navigate(R.id.fragment_medical_reports, bundle)
                    toolbar.visibility = View.GONE // Ascunde Toolbar
                    Toast.makeText(this, "Medical reports Doctor", Toast.LENGTH_SHORT).show()
                }
                R.id.navigation_settings -> {
                    navController.navigate(R.id.fragment_settings)
                    Toast.makeText(this, "Settings Doctor", Toast.LENGTH_SHORT).show()
                }


                else -> return@setOnItemSelectedListener false
            }
            true
        }
    }

    private fun setBottomNavigationPatient(navView: BottomNavigationView, navController: NavController, userId: String) {
        navView.menu.clear()
        navView.inflateMenu(R.menu.bottom_nav_menu_patient) // Încarcă meniul pentru pacienți
        Toast.makeText(this, "Patient Menu Loaded", Toast.LENGTH_SHORT).show()

        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_patient_results -> {
                    val bundle = Bundle()
                    bundle.putString("patientId", userId) // Transmite doctorId către fragment
                    navController.navigate(R.id.fragment_results_patient_test, bundle)
                    Toast.makeText(this, "Management Doctor", Toast.LENGTH_SHORT).show()
                }
                R.id.navigation_settings -> {
                    navController.navigate(R.id.fragment_settings)
                    Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show()
                }
                else -> return@setOnItemSelectedListener false
            }
            true
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

}
