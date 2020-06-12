package rr.rms.chord

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_cluster.*
import rr.rms.R

class ClusterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cluster)
        setSupportActionBar(toolbar)

        addCluster.setOnClickListener {
            var cluster = Cluster()
        }
    }

}
