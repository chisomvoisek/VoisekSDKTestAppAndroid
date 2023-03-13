package com.example.voiseksdktestappandroid

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.voisek_sdk.VoisekSdk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.permissionx.guolindev.PermissionX
import com.permissionx.guolindev.PermissionX.init

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        var buttonInitSDK = findViewById<Button>(R.id.buttonInitSDK)
        var editTextPhoneNumber = findViewById<EditText>(R.id.editTextPhoneNumber)
        var buttonSendRegistrationCode = findViewById<Button>(R.id.buttonSendRegistrationCode)
        var ediTextVerifyCode = findViewById<EditText>(R.id.ediTextVerifyCode)
        var buttonVerify = findViewById<Button>(R.id.buttonVerify)
        var buttonEnableAntiVishing = findViewById<Button>(R.id.buttonEnableAntiVishing)
        var buttonEnableAccountTakeOver = findViewById<Button>(R.id.buttonEnableAccountTakeOver)
        var buttonManualCheck = findViewById<Button>(R.id.buttonManualCheck)
        var buttonCallOnGoing = findViewById<Button>(R.id.buttonCallOnGoing)
        var editTextMessage = findViewById<EditText>(R.id.editTextMessage)
        var buttonSMSCheck = findViewById<Button>(R.id.buttonSMSCheck)
        var buttonDeleteAccount = findViewById<Button>(R.id.buttonDeleteAccount)


       // buttonInitSDK.isEnabled = true
        editTextPhoneNumber.isEnabled = false
        buttonSendRegistrationCode.isEnabled = false
        ediTextVerifyCode.isEnabled = false
        buttonVerify.isEnabled = false;
        buttonEnableAntiVishing.isEnabled = false;
        buttonEnableAccountTakeOver.isEnabled = false;
        buttonManualCheck.isEnabled = false;
        buttonDeleteAccount.isEnabled = false;
        buttonCallOnGoing.isEnabled = false;
        editTextMessage.isEnabled = false;
        buttonSMSCheck.isEnabled = false;
        buttonDeleteAccount.isEnabled = false;



        buttonInitSDK.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {

                this@MainActivity.runOnUiThread(Runnable {

                PermissionX.init(this@MainActivity)
                    .permissions(Manifest.permission.READ_PHONE_STATE, Manifest.permission.ANSWER_PHONE_CALLS, Manifest.permission.READ_CALL_LOG)
                    .request { allGranted, grantedList, deniedList ->
                        if (allGranted) {
                            Toast.makeText(this@MainActivity, "All permissions are granted", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this@MainActivity, "These permissions are denied: $deniedList", Toast.LENGTH_LONG).show()
                        }
                    }

//        requestRole();


                    var isPhoneOnDevice = VoisekSdk.isPhoneNumberOnDevice(this@MainActivity)

                    if (isPhoneOnDevice == false) {//if device phone number is not already registered on device

                        var accountId = "JC007948"

                        var returnString = VoisekSdk.initSDK(
                            this@MainActivity, accountId
                        )


                        if (returnString.equals("SDK_INITIALIZED")) {


                            this@MainActivity.runOnUiThread(Runnable {
                                buttonSendRegistrationCode.isEnabled = true;
                                buttonInitSDK.isEnabled = false;
                                editTextPhoneNumber.isEnabled = true


                                Toast.makeText(
                                    this@MainActivity,
                                    "SDK_INITIALIZED",
                                    Toast.LENGTH_LONG
                                ).show()

                            })

                            //we need to update the blacklist and whitelist on local db
                            VoisekSdk.updateAntiVishing(this@MainActivity)


                        } else {
                            this@MainActivity.runOnUiThread(Runnable {
                                Toast.makeText(
                                    this@MainActivity,
                                    "SDK_NOT_INITIALIZED",
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                            })

                        }


                    } else { //if device phone number is already onboarded, then skip onboarding

                        buttonSendRegistrationCode.isEnabled = false;
                        buttonInitSDK.isEnabled = false;
                        editTextPhoneNumber.isEnabled = false
                        ediTextVerifyCode.isEnabled = false;
                        editTextMessage.isEnabled = true;



                        buttonEnableAntiVishing.isEnabled = true
                        buttonEnableAccountTakeOver.isEnabled = true
                        buttonManualCheck.isEnabled = true
                        buttonCallOnGoing.isEnabled = true
                        buttonSMSCheck.isEnabled = true
                        buttonDeleteAccount.isEnabled = true


                        //we need to update the blacklist and whitelist on local db
                        VoisekSdk.updateAntiVishing(this@MainActivity)

                        Toast.makeText(
                            this@MainActivity,
                            "Phone Number on Device is already onboarded",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                })
            }

        }

        buttonSendRegistrationCode.setOnClickListener {

            GlobalScope.launch(Dispatchers.IO) {


                var phone = editTextPhoneNumber.text
                var response = VoisekSdk.sendRegisterCode(this@MainActivity, "${phone}")
                Log.d("Resp","${response}")
                this@MainActivity.runOnUiThread(Runnable {
                    println("RESPONSE:::"+ response.toString())
                    val ALREADY_REGISTERED = "ALREADY_REGISTERED"
                    val NEWLY_REGISTERED = "REGISTRATION_ACCEPTED"

                    if(response == NEWLY_REGISTERED) {

                        editTextPhoneNumber.isEnabled = false
                        buttonVerify.isEnabled = true
                        buttonSendRegistrationCode.isEnabled = false
                        ediTextVerifyCode.isEnabled = true;

                    }else if(response == ALREADY_REGISTERED){

                        editTextPhoneNumber.isEnabled = false
                        buttonVerify.isEnabled = false
                        buttonSendRegistrationCode.isEnabled = false
                        ediTextVerifyCode.isEnabled = false;
                        buttonEnableAntiVishing.isEnabled = true;
                        buttonEnableAccountTakeOver.isEnabled = true;
                        buttonDeleteAccount.isEnabled = true;
                        buttonManualCheck.isEnabled = true;
                        buttonSMSCheck.isEnabled = true;
                        editTextMessage.isEnabled = true;


                    }
                    else{

                        Toast.makeText(this@MainActivity, "error here::: ${response}" ,Toast.LENGTH_SHORT).show()

                    }
                })
            }

        }



        buttonVerify.setOnClickListener{
            GlobalScope.launch(Dispatchers.IO) {
                var code = ediTextVerifyCode.text
                var phone = editTextPhoneNumber.text
                val retResult = VoisekSdk.verifyCode(this@MainActivity, "${phone}", "${code}")

                if(retResult == "DEVICE_REGISTERED") {
//                    if (token) {
                    this@MainActivity.runOnUiThread(Runnable {
                        buttonVerify.isEnabled = false;
                        ediTextVerifyCode.isEnabled = false;
                        buttonEnableAntiVishing.isEnabled = true;
                        buttonEnableAccountTakeOver.isEnabled = true;
                        buttonDeleteAccount.isEnabled = true;
                        buttonManualCheck.isEnabled = true;
                        buttonCallOnGoing.isEnabled = true;
                        buttonSMSCheck.isEnabled = true;
                        editTextMessage.isEnabled = true;


                        Toast.makeText(this@MainActivity, "${retResult.toString()}" ,Toast.LENGTH_LONG).show()


                    })


                }else{
                    this@MainActivity.runOnUiThread(Runnable {
                        Toast.makeText(this@MainActivity, "${retResult.toString()}" ,Toast.LENGTH_LONG).show()
                    })
                }
            }
        }



        buttonEnableAntiVishing.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                val enable = VoisekSdk.enableAntiVishing(this@MainActivity);

                this@MainActivity.runOnUiThread(Runnable {
                    Toast.makeText(
                        this@MainActivity,
                        "$enable",
                        Toast.LENGTH_SHORT
                    ).show()
                })

            }
        }



        buttonEnableAccountTakeOver.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                val otp = "454545"
                val validationInfo = VoisekSdk.antiAccountTakeoverValidation(this@MainActivity, otp);

                this@MainActivity.runOnUiThread(Runnable {
                    Toast.makeText(this@MainActivity, "$validationInfo" ,Toast.LENGTH_LONG).show()
                })

            }
        }



        buttonManualCheck.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                val isIt = VoisekSdk.manualCheck(this@MainActivity)
                this@MainActivity.runOnUiThread(Runnable {
                    if(isIt == null)
                    {
                        Toast.makeText(this@MainActivity, "No disponible", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@MainActivity, "${isIt}", Toast.LENGTH_LONG).show()
                    }
                })
            }
        }

        buttonSMSCheck.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {

                var mesg = ediTextVerifyCode.text
                val validationInfo = VoisekSdk.antiSmishingValidation(this@MainActivity, "${mesg}")

                this@MainActivity.runOnUiThread(Runnable {
                    Toast.makeText(this@MainActivity, "${validationInfo}" ,Toast.LENGTH_SHORT).show()
                })

            }
        }

        buttonCallOnGoing.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                val Is = VoisekSdk.isCallOnGoing(this@MainActivity)
                this@MainActivity.runOnUiThread(Runnable {
                    Toast.makeText(this@MainActivity, "${Is}", Toast.LENGTH_LONG).show()
                })
            }
        }

        buttonDeleteAccount.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                val delete = VoisekSdk.deleteAccount(this@MainActivity)
                this@MainActivity.runOnUiThread(Runnable {
                    Toast.makeText(this@MainActivity, "${delete}", Toast.LENGTH_LONG).show()
                })
            }
        }



    }
}