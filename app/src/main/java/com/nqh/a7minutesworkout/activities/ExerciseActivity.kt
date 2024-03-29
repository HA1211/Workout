package com.nqh.a7minutesworkout.activities

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.nqh.a7minutesworkout.Constants
import com.nqh.a7minutesworkout.adapter.ExerciseAdapter
import com.nqh.a7minutesworkout.databinding.ActivityExerciseBinding
import com.nqh.a7minutesworkout.databinding.DialogCustomBackConfirmationBinding
import com.nqh.a7minutesworkout.models.ExerciseModel
import java.util.Locale

class ExerciseActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityExerciseBinding

    //speak
    private lateinit var tts: TextToSpeech

    private var restTimer: CountDownTimer? = null
    private var restProgress = 0


    private var exerciseTimer: CountDownTimer? = null
    private var exerciseProgress = 0

    private var exerciseList: ArrayList<ExerciseModel>? = null
    private var currentExercisePosition = -1

    private lateinit var exerciseAdapter: ExerciseAdapter

    private var breakTime: Long = 1
    private var exerciseTimePractice: Long = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExerciseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarExercise)


        //speak
        tts = TextToSpeech(this, this)

        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        binding.toolbarExercise.setNavigationOnClickListener {
            customDialogForBack()
        }

        exerciseList = Constants.defaultExerciseList()

        setupRestView()

        setupExerciseStatusRecyclerView()

    }

    private fun customDialogForBack() {
        val customDialog = Dialog(this)
        val dialogBinding = DialogCustomBackConfirmationBinding.inflate(layoutInflater)
        customDialog.setContentView(dialogBinding.root)

        customDialog.setCanceledOnTouchOutside(false) //không thể hủy khi nhấn vùng bên ngoài button

        dialogBinding.btnYes.setOnClickListener {
            this@ExerciseActivity.finish()
            customDialog.dismiss()
        }
        dialogBinding.btnNo.setOnClickListener {
            customDialog.dismiss()
        }
        customDialog.show()
    }

    override fun onBackPressed() {
        customDialogForBack()
    }


    private fun setupExerciseStatusRecyclerView() {
        exerciseAdapter = ExerciseAdapter(exerciseList!!)
        binding.rvExerciseStatus.adapter = exerciseAdapter
    }

    private fun setupRestView() {

        binding.flProgressBar.visibility = View.VISIBLE
        binding.tvRestTimer.visibility = View.VISIBLE
        binding.tvTitle.visibility = View.VISIBLE
        binding.tvExercise.visibility = View.VISIBLE

        binding.flExercise.visibility = View.INVISIBLE
        binding.tvExerciseName.visibility = View.INVISIBLE
        binding.ivImage.visibility = View.INVISIBLE


        if (restTimer != null) {
            restTimer?.cancel()
            restProgress = 0
        }

        binding.tvExercise.text = exerciseList!![currentExercisePosition + 1].getName()

        setRestProgressBar()

    }


    private fun setRestProgressBar() {
        binding.progressBar.progress = restProgress

        restTimer = object : CountDownTimer(breakTime * 1000, 1000) {
            override fun onTick(p0: Long) {
                restProgress++
                binding.progressBar.progress = breakTime.toInt() - restProgress
                binding.tvTimer.text = (breakTime.toInt() - restProgress).toString()

            }

            override fun onFinish() {
                currentExercisePosition++
                exerciseList!![currentExercisePosition].setSelected(true)
                exerciseAdapter.notifyDataSetChanged()

                setupExerciseView()
            }
        }.start()
    }


    private fun setupExerciseView() {

        binding.flProgressBar.visibility = View.INVISIBLE
        binding.tvRestTimer.visibility = View.INVISIBLE
        binding.tvTitle.visibility = View.INVISIBLE
        binding.tvExercise.visibility = View.INVISIBLE

        binding.flExercise.visibility = View.VISIBLE
        binding.tvExerciseName.visibility = View.VISIBLE
        binding.ivImage.visibility = View.VISIBLE


        //speak
        speakOut(exerciseList!![currentExercisePosition].getName())

        if (exerciseTimer != null) {
            exerciseTimer?.cancel()
            exerciseProgress = 0
        }

        binding.ivImage.setImageResource(exerciseList!![currentExercisePosition].getImage())
        binding.tvExerciseName.text = exerciseList!![currentExercisePosition].getName()

        setExerciseProgressBar()
    }


    private fun setExerciseProgressBar() {
        binding.progressBarExercise.progress = exerciseProgress

        exerciseTimer = object : CountDownTimer(exerciseTimePractice * 1000, 1000) {
            override fun onTick(p0: Long) {
                exerciseProgress++
                binding.progressBarExercise.progress = exerciseTimePractice.toInt() - exerciseProgress
                binding.tvTimerExercise.text = (exerciseTimePractice.toInt() - exerciseProgress).toString()
            }

            override fun onFinish() {
                if (currentExercisePosition < exerciseList!!.size - 1) {
                    exerciseList!![currentExercisePosition].setSelected(false)
                    exerciseList!![currentExercisePosition].setCompleted(true)
                    exerciseAdapter?.notifyDataSetChanged()
                    setupRestView()
                } else {
                    finish()
                    startActivity(Intent(this@ExerciseActivity, FinishActivity::class.java))
                }
            }
        }.start()
    }

    override fun onInit(status: Int) {
        //check (chắc là không có cũng được)
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.ENGLISH)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This language is no available")
            }
        } else {
            Log.e("TTS", "Failed!")
        }
    }

    //speak function
    private fun speakOut(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

}