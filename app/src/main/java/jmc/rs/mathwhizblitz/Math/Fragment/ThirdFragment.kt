package jmc.rs.mathwhizblitz.Math.Fragment

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import jmc.rs.mathwhizblitz.Math.Model.MathQuestionModel
import jmc.rs.mathwhizblitz.R
import jmc.rs.mathwhizblitz.databinding.FragmentThirdBinding

class ThirdFragment : Fragment() {

    private var _binding: FragmentThirdBinding? = null
    private val binding get() = _binding!!
    private lateinit var databaseReference: DatabaseReference

    private var currentQuestionIndex = 0
    private var score = 0
    private lateinit var mathQuestionDataList: List<MathQuestionModel>
    private lateinit var progressBar: ProgressBar
    private lateinit var countDownTimer: CountDownTimer
    private var timeLeftInMillis: Long = 0
    private val countdownInterval: Long = 1000 // 1 second interval

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentThirdBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun getFragmentBinding(): FragmentThirdBinding? {
        return if (isAdded) _binding else null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        databaseReference = FirebaseDatabase.getInstance().getReference("math")
        progressBar = getFragmentBinding()?.progressBar!!
        retrieveDataFromFirebase()
    }

    private fun retrieveDataFromFirebase() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        mathQuestionDataList = dataSnapshot.children.mapNotNull {
                            it.getValue(MathQuestionModel::class.java)
                        }
                        mathQuestionDataList = mathQuestionDataList.drop(1)

                        for (data in mathQuestionDataList) {
                            Log.d(
                                "FirebaseData",
                                "Question: ${data.question}, Answer: ${data.answer}"
                            )
                        }
                        // Shuffle the questions
                        shuffleQuestions()

                        progressBar.visibility = View.GONE
                        binding.questionLayout.visibility = View.VISIBLE

                        loadQuestion()
                    } catch (e: Exception) {
                        Log.e("Exception", e.toString())
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
                Log.w("FirebaseError", "Failed to read value.", databaseError.toException())
            }
        })
    }

    private fun loadQuestion() {
        resetTimer()

        if (currentQuestionIndex < mathQuestionDataList.size) {
            val currentQuestion = mathQuestionDataList[currentQuestionIndex]

            binding.questionNumber.text =
                getString(R.string.questionss, currentQuestionIndex + 1, mathQuestionDataList.size)
            binding.question.text = currentQuestion.question
            binding.optionsLinearLayout.removeAllViews()

            val allDescriptions = currentQuestion.options
            val shuffledOptions = allDescriptions.shuffled()

            for (option in shuffledOptions) {
                val button = createButton(option)
                binding.optionsLinearLayout.addView(button)
            }
            startTimer()
        } else {
            // Show the final score if all questions have been answered
            showScore()
        }
    }

    private fun shuffleQuestions() {
        mathQuestionDataList = mathQuestionDataList.shuffled().take(15)
    }

    private fun createButton(option: String): Button {
        val button = Button(requireContext())
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        button.layoutParams = layoutParams
        button.text = option
        layoutParams.bottomMargin = resources.getDimensionPixelSize(R.dimen.your_button_padding)
        val defaultTextSize = resources.configuration.fontScale
        val scaleFactor = 20f
        button.textSize = defaultTextSize * scaleFactor
        button.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        button.background = ContextCompat.getDrawable(requireContext(), R.drawable.btn_bg)
        button.setPadding(
            resources.getDimensionPixelSize(R.dimen.your_button_padding),
            resources.getDimensionPixelSize(R.dimen.your_button_padding),
            resources.getDimensionPixelSize(R.dimen.your_button_padding),
            resources.getDimensionPixelSize(R.dimen.your_button_padding)
        )
        button.setOnClickListener {
            onButtonClicked(button)
        }

        return button
    }

    private fun onButtonClicked(button: Button) {
        resetTimer()

        val selectedAnswer = button.text.toString()
        val correctAnswer = mathQuestionDataList[currentQuestionIndex].answer

        if (selectedAnswer == correctAnswer) {
            button.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.btn_bg_correct)
            score++
        } else {
            button.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.btn_bg_incorrect)
            val correctButton = binding.optionsLinearLayout.findViewWithTag<Button>(correctAnswer)
            correctButton?.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.btn_bg_correct)
        }

        disableButtons()

        Handler().postDelayed({
            currentQuestionIndex++
            loadQuestion()
            resetButtonBackgrounds()
            enableButtons()
        }, 1000) // 1 second delay
    }

    private fun disableButtons() {
        binding.optionsLinearLayout.children.forEach { (it as? Button)?.isEnabled = false }
    }

    private fun enableButtons() {
        binding.optionsLinearLayout.children.forEach { (it as? Button)?.isEnabled = true }
    }

    private fun resetButtonBackgrounds() {
        binding.optionsLinearLayout.children.forEach {
            (it as? Button)?.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.btn_bg)
        }
    }

    private fun showScore() {
        binding.questionLayout.visibility = View.GONE
        binding.score.visibility = View.VISIBLE
        binding.scoretv.text = getString(R.string.quiz_completed, score, mathQuestionDataList.size)
        binding.optionsLinearLayout.removeAllViews()
        Log.e("score", score.toString())

        binding.submitButton.text = getString(R.string.play_again)
        binding.submitButton.setOnClickListener {
            resetQuiz()
        }
    }

    private fun resetQuiz() {
        currentQuestionIndex = 0
        score = 0
        findNavController().navigate(R.id.action_ThirdFragment_to_FirstFragment)
    }

    private fun startTimer() {
        timeLeftInMillis = 20000 // 5 seconds

        val progressBar = binding.progressBarTimer
        progressBar.max = timeLeftInMillis.toInt()
        progressBar.progress = timeLeftInMillis.toInt()

        countDownTimer = object : CountDownTimer(timeLeftInMillis, countdownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                progressBar.progress = timeLeftInMillis.toInt()
            }

            override fun onFinish() {
                onTimerFinish()
            }
        }.start()
    }

    private fun resetTimer() {
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }
    }

    private fun onTimerFinish() {
        val currentBinding = getFragmentBinding()
        if (currentBinding != null) {
            currentQuestionIndex++
            loadQuestion()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
