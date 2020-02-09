package study.junghoon.app.aac_study.sample1

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import study.junghoon.app.aac_study.R
import study.junghoon.app.aac_study.databinding.ActivitySample1Binding

class Sample1Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_sample1)
        val binding : ActivitySample1Binding=
            DataBindingUtil.setContentView(this, R.layout.activity_sample1)

        binding.name = "Your name"
        binding.lastName = "Your last name"
    }

    fun onLike(view: View) {}
}
