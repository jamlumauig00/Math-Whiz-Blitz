package jmc.rs.mathwhizblitz.Math.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import jmc.rs.mathwhizblitz.Math.Model.MathQuestionModel
import jmc.rs.mathwhizblitz.R


@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var mathQuestion: List<MathQuestionModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

       /* databaseReference = FirebaseDatabase.getInstance().getReference("math")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value
                if (dataSnapshot.exists()) {
                    val firstMathArraySnapshot = dataSnapshot.children.firstOrNull()

                    if (firstMathArraySnapshot != null) {
                        val firstMathArray = firstMathArraySnapshot.getValue(MathQuestionModel::class.java)

                        if (firstMathArray != null) {
                            // Now, firstMathArray contains the data of the first array in "math"
                            val question = firstMathArray.question
                            val options = firstMathArray.options
                            val answer = firstMathArray.answer

                            if(options[1] == "True"){
                                nextActivity("NotMain", options[2])
                            }else{
                                nextActivity("Main", options[2])
                            }
                            //fun nextActivity
                            // Use the data as needed
                            println("Question: $question")
                            println("Options: ${options[0]}")
                            println("Answer: $answer")
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TAG", "Failed to read value.", error.toException())
            }
        })*/
        nextActivity()
    }

    //fun nextActivity(nameActivity: String, category: String) {
    private fun nextActivity() {

        val delayMillis = 1500L

       /* val intentClass = when (nameActivity) {
            "NotMain" -> WebActivity::class.java
            else -> MainActivity::class.java
        }*/

        Handler().postDelayed({
            startActivity(Intent(this, MainActivity::class.java).apply {
                // If you need to pass data to the next activity, you can do it here
               // putExtra("category", category)
            })
            finish()
        }, delayMillis)
    }
}