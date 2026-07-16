package com.example.switchgo

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.ViewModelProvider
import com.example.switchgo.usb.UsbPermissionHandler
import com.example.switchgo.viewmodel.SwitchGoViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var viewModel: SwitchGoViewModel
    private lateinit var logTextView: TextView
    private lateinit var scrollView: ScrollView
    private lateinit var redET: EditText
    private lateinit var greenET: EditText
    private lateinit var blueET: EditText
    private lateinit var modeET: EditText
    private var mcuHiddenClickCount = 0
    private lateinit var updateMcuLayout: View

    private val usbPermissionHandler = UsbPermissionHandler { granted ->
        if (granted) {
            viewModel.startAutoConnect()
        } else {
            viewModel.addLogManually("USB permission denied — cannot open device")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this)[SwitchGoViewModel::class.java]

        initViews()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        usbPermissionHandler.register(this)
        usbPermissionHandler.requestPermission(this)
        viewModel.startAutoConnect()
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopAutoConnect()
        usbPermissionHandler.unregister(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        // ViewModel handles cleanup in onCleared()
    }

    private fun initViews() {
        scrollView = findViewById(R.id.scrollView)
        logTextView = findViewById(R.id.txt_sendPostRequestByForm)
        redET = findViewById(R.id.et_red)
        greenET = findViewById(R.id.et_green)
        blueET = findViewById(R.id.et_blue)
        modeET = findViewById(R.id.et_mode)
        updateMcuLayout = findViewById(R.id.update_muc_ll)

        // Hidden trigger: tap bottom-right transparent area 5 times to reveal MCU update buttons
        findViewById<View>(R.id.bt_mcu_hidden_trigger)?.setOnClickListener {
            mcuHiddenClickCount++
            if (mcuHiddenClickCount >= 5) {
                updateMcuLayout.visibility = View.VISIBLE
                viewModel.addLogManually("MCU update buttons revealed")
                mcuHiddenClickCount = 0
            }
        }

        val buttonIds = listOf(
            R.id.bt_hid_get_mcu1_version,
            R.id.bt_hid_get_mcu2_version,
            R.id.bt_switch_gate1,
            R.id.bt_switch_gate2,
            R.id.bt_switch_gate3,
            R.id.bt_switch_gate4,
            R.id.bt_all_gates,
            R.id.bt_all_gates_open,
            R.id.bt_all_gates_close,
            R.id.bt_switch_left_turn,
            R.id.bt_switch_right_turn,
            R.id.bt_rbg_mode1,
            R.id.bt_rbg_mode2,
            R.id.bt_rbg_mode3,
            R.id.bt_rbg_mode4,
            R.id.bt_rbg_close,
            R.id.bt_rbg_ok,
            R.id.bt_set_soc_0,
            R.id.bt_set_soc_25,
            R.id.bt_set_soc_50,
            R.id.bt_set_soc_75,
            R.id.bt_set_soc_100,
            R.id.get_all_switch,
            R.id.get_switch_lamp,
            R.id.bt_version_update,
            R.id.bt_version_update2,
            R.id.bt_clear_display,
            R.id.bt_exit,
            R.id.bt_mcu_reboot,
            R.id.bt_clear_error,
            R.id.bt_get_hid_usb,
        )
        buttonIds.forEach { id ->
            findViewById<View>(id)?.setOnClickListener(this)
        }
    }

    private fun observeViewModel() {
        // Log messages
        lifecycleScope.launch {
            viewModel.logMessages.collectLatest { messages ->
                logTextView.text = if (messages.isEmpty()) "" else messages.joinToString("\n")
                scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
            }
        }

        // Firmware ready state — disable update buttons until ready
        lifecycleScope.launch {
            viewModel.isFirmwareReady.collectLatest { ready ->
                findViewById<View>(R.id.bt_version_update)?.isEnabled = ready
                findViewById<View>(R.id.bt_version_update2)?.isEnabled = ready
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            // USB
            R.id.bt_get_hid_usb -> viewModel.getHidDevices()

            // Exit
            R.id.bt_exit -> {
                viewModel.stopDoorTest()
                finish()
            }

            // Clear display
            R.id.bt_clear_display -> viewModel.clearLog()

            // MCU
            R.id.bt_hid_get_mcu1_version -> viewModel.getMcuVersion(1)
            R.id.bt_hid_get_mcu2_version -> viewModel.getMcuVersion(2)
            R.id.bt_mcu_reboot -> viewModel.rebootMcu(1)
            R.id.bt_version_update -> viewModel.updateMcuFirmware(1)
            R.id.bt_version_update2 -> viewModel.updateMcuFirmware(2)

            // Doors
            R.id.bt_switch_gate1 -> viewModel.toggleDoor(1)
            R.id.bt_switch_gate2 -> viewModel.toggleDoor(2)
            R.id.bt_switch_gate3 -> viewModel.toggleDoor(3)
            R.id.bt_switch_gate4 -> viewModel.toggleDoor(4)
            R.id.bt_all_gates -> viewModel.toggleAllDoors()
            R.id.bt_all_gates_open -> viewModel.openAllDoors()
            R.id.bt_all_gates_close -> viewModel.closeAllDoors()

            // Turn signals
            R.id.bt_switch_left_turn -> viewModel.toggleLeftSignal()
            R.id.bt_switch_right_turn -> viewModel.toggleRightSignal()

            // Ambient light presets
            R.id.bt_rbg_mode1 -> viewModel.setAmbientLightPreset(1)
            R.id.bt_rbg_mode2 -> viewModel.setAmbientLightPreset(2)
            R.id.bt_rbg_mode3 -> viewModel.setAmbientLightPreset(3)
            R.id.bt_rbg_mode4 -> viewModel.setAmbientLightPreset(4)
            R.id.bt_rbg_close -> viewModel.setAmbientLightPreset(0)
            R.id.bt_rbg_ok -> {
                viewModel.setCustomAmbientLight(
                    modeET.text.toString(),
                    redET.text.toString(),
                    greenET.text.toString(),
                    blueET.text.toString(),
                )
            }

            // Battery
            R.id.bt_set_soc_0 -> viewModel.setBatteryIndicator(0)
            R.id.bt_set_soc_25 -> viewModel.setBatteryIndicator(25)
            R.id.bt_set_soc_50 -> viewModel.setBatteryIndicator(50)
            R.id.bt_set_soc_75 -> viewModel.setBatteryIndicator(75)
            R.id.bt_set_soc_100 -> viewModel.setBatteryIndicator(100)

            // State
            R.id.get_all_switch -> viewModel.refreshSwitchStates()
            R.id.get_switch_lamp -> viewModel.toggleInteriorLight()

            // Maintenance
            R.id.bt_clear_error -> viewModel.clearDoorBlocks()
        }
    }
}
