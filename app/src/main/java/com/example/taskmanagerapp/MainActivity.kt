package com.example.taskmanagerapp

import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.taskmanagerapp.databinding.ActivityMainBinding
import com.example.taskmanagerapp.viewmodel.TaskViewModel
import com.example.taskmanagerapp.viewmodel.TaskViewModelFactory

class MainActivity : AppCompatActivity() {
	private lateinit var binding: ActivityMainBinding
	private lateinit var navController: NavController
	private lateinit var appBarConfiguration: AppBarConfiguration

	private var doubleBackPressedToExit = false

	val taskViewModel: TaskViewModel by viewModels {
		TaskViewModelFactory((application as MyApp).repository)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)
		setSupportActionBar(binding.toolbar)

		val navHostFragment =
			supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHost

		navController = navHostFragment.navController

		appBarConfiguration = AppBarConfiguration(
			navController.graph
		)


		setupActionBarWithNavController(navController, appBarConfiguration)
		supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)

		onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
			override fun handleOnBackPressed() {
				if (navController.currentDestination?.id == R.id.allTasksFragment) {
					if (doubleBackPressedToExit) {
						finish()
					} else {
						doubleBackPressedToExit = true
						Toast.makeText(
							this@MainActivity,
							"Press back again to exit!",
							Toast.LENGTH_SHORT
						).show()
						Handler(Looper.getMainLooper()).postDelayed({
							doubleBackPressedToExit = false
						}, 2000)
					}
				} else {
					navController.navigateUp()
				}
			}

		})
	}

	override fun onSupportNavigateUp(): Boolean {
		val navController = findNavController(R.id.fragmentContainerView)
		return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
	}

	private fun hideKeyboard(view: View) {
		val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
		imm.hideSoftInputFromWindow(view.windowToken, 0)
	}

	override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
		if (ev.action == MotionEvent.ACTION_DOWN) {
			val v = currentFocus
			if (v is EditText) {
				val outRect = Rect()
				v.getGlobalVisibleRect(outRect)
				if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
					v.clearFocus()
					hideKeyboard(v)
				}
			}
		}
		return super.dispatchTouchEvent(ev)
	}
}