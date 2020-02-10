package study.junghoon.app.aac_study.sample1

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import study.junghoon.app.aac_study.R
import study.junghoon.app.aac_study.databinding.ActivitySample1Binding
import study.junghoon.app.aac_study.plain.data.SimpleViewModel

class Sample1Activity : AppCompatActivity() {

    private val viewModel by lazy { ViewModelProviders.of(this).get(SimpleViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_sample1)
        val binding : ActivitySample1Binding=
            DataBindingUtil.setContentView(this, R.layout.activity_sample1)

        /**
         * 이를 통해 이전 액티비티에서 메서드를 통해 초기화했던 부분을 생략할 수 있다.
         */
        binding.viewmodel = viewModel
    }
}
