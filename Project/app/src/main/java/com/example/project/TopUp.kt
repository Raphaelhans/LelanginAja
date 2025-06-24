package com.example.project

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.project.database.App
import com.example.project.database.App.Companion.baseUrl
import com.example.project.database.App.Companion.key
import com.example.project.database.dataclass.MidtransResponse
import com.example.project.database.dataclass.Payment
import com.example.project.databinding.TopupBinding
import com.midtrans.sdk.corekit.core.MidtransSDK
import java.text.NumberFormat
import java.util.Locale
import com.midtrans.sdk.corekit.callback.TransactionFinishedCallback
import com.midtrans.sdk.uikit.api.model.TransactionResult
import com.midtrans.sdk.uikit.SdkUIFlowBuilder
import com.midtrans.sdk.uikit.external.UiKitApi
import com.midtrans.sdk.uikit.internal.util.UiKitConstants

class TopUp: BaseClass() {
    private lateinit var binding: TopupBinding
    val viewModel by viewModels<UserViewModel>()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val midtransLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result?.resultCode == RESULT_OK) {
            result.data?.let { data ->
                val transactionResult = data.getParcelableExtra(
                    UiKitConstants.KEY_TRANSACTION_RESULT,
                    TransactionResult::class.java
                )
                transactionResult?.let { res ->
                    Log.d("Midtrans", "Transaction Result: ${res.paymentType}")
                    Log.d("Midtrans", "Transaction Result: ${res.transactionId}")

                    when (res.status) {
                        "success" -> {
                            Toast.makeText(this, "Payment Success: ${res.transactionId}", Toast.LENGTH_SHORT).show()
                            viewModel.topupPayment(
                                res.transactionId ?: "",
                                res.paymentType ?: "",
                                res.status,
                            )
                        }
                        "pending" -> {
                            Toast.makeText(this, "Payment Pending", Toast.LENGTH_SHORT).show()
//                            finish()
                        }
                        "failed" -> {
                            Toast.makeText(this, "Payment Failed", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        "invalid" -> {
                            Toast.makeText(this, "Invalid Transaction", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        else -> {
                            Toast.makeText(this, "Payment Success", Toast.LENGTH_SHORT).show()
                            viewModel.topupPayment(
                                res.transactionId ?: "",
                                res.paymentType ?: "",
                                "success",
                            )

                        }
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TopupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.currUser.observe(this) { user ->
            binding.btntopup.setOnClickListener {
                if (binding.addbalanceTxt.text.toString().isNotEmpty()){
                    val amount = binding.addbalanceTxt.text.toString().replace(".", "").toInt()
                    if (amount >= 10000) {
                        viewModel.createMidtransTransaction(amount)
                    }
                    else{
                        Toast.makeText(this, "Minimum top up is Rp10.000", Toast.LENGTH_SHORT).show()
                    }
                }
                else{
                    Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
                }
            }

            binding.backbtn.setOnClickListener {
                val intent = Intent(this, HomeUser::class.java)
                intent.putExtra("email", viewModel.currUser.value?.email)
                startActivity(intent)
            }
        }

        binding.addbalanceTxt.addTextChangedListener(object :TextWatcher{
            private var current = ""
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                binding.addbalanceTxt.removeTextChangedListener(this)
                val rawInput = s.toString().replace("[^0-9]".toRegex(), "")

                if (rawInput.isNotEmpty()) {
                    try {
                        val number = rawInput.toLong()

                        val formatter = NumberFormat.getNumberInstance(Locale("in", "ID"))
                        formatter.minimumFractionDigits = 0
                        formatter.maximumFractionDigits = 0
                        val formatted = formatter.format(number)

                        if (formatted != current) {
                            current = formatted
                            binding.addbalanceTxt.setText(formatted)
                            binding.addbalanceTxt.setSelection(formatted.length)
                        }
                    } catch (e: NumberFormatException) {
                        binding.addbalanceTxt.setText(current)
                    }
                } else {
                    current = ""
                    binding.addbalanceTxt.setText("")
                }

                binding.addbalanceTxt.addTextChangedListener(this)
            }

        })

        viewModel.snapRedirectToken.observe(this) { token ->
            UiKitApi.getDefaultInstance().startPaymentUiFlow(this,midtransLauncher,token)
        }

    }

    override fun onStart() {
        super.onStart()
        val email = intent.getStringExtra("email")
        if (email != null) {
            viewModel.getCurrUser(email)
        }
    }

}