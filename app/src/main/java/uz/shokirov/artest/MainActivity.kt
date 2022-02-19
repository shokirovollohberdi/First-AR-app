package uz.shokirov.artest

import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.google.ar.core.Anchor
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.SkeletonNode
import com.google.ar.sceneform.animation.ModelAnimator
import com.google.ar.sceneform.rendering.AnimatorImpl
import com.google.ar.sceneform.rendering.LullModel
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_main.*
import uz.shokirov.artest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var arFragment:ArFragment
    private lateinit var model:Uri
    private var renderable:ModelRenderable?=null
    private var animator: ModelAnimator?=null
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        arFragment = sceneform_fragment as ArFragment
        model = Uri.parse("model_fight.sfb")

        arFragment.setOnTapArPlaneListener { hitResult, plane, motionEvent ->
            if (plane.type!=Plane.Type.HORIZONTAL_UPWARD_FACING){
                return@setOnTapArPlaneListener
            }
            var anchor = hitResult.createAnchor()
            placeObject(arFragment,anchor,model)

        }
        animate_kick_button.setOnClickListener {
            animateModel("Character|Kick")
        }
        animate_boxing_button.setOnClickListener {
            animateModel("Character|Boxing")
        }
        animate_idle_button.setOnClickListener {
            animateModel("Character|Idle")
        }


    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun animateModel(name: String) {
        animator?.let {it->
             if (it.isRunning){
                 it.end()
             }
        }
        renderable?.let { modelRenderable ->
            val data = modelRenderable.getAnimationData(name)
            animator = ModelAnimator(data,modelRenderable)
            animator?.start()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun placeObject(arFragment: ArFragment, anchor: Anchor?, model: Uri?) {
        ModelRenderable.builder().setSource(arFragment.context,model).build()
                .thenAccept {
                    renderable = it
                    addtoScene(arFragment,anchor,it)
                }
                .exceptionally {
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage(it.message.toString()).setTitle("Error")
                    var dialog = builder.create()
                    dialog.show()
                    return@exceptionally null
                }
    }

    private fun addtoScene(arFragment: ArFragment, anchor: Anchor?, it: ModelRenderable?) {
        val anchorNode = AnchorNode(anchor)
        val skeletonNode = SkeletonNode()
        skeletonNode.renderable = renderable
        val node = TransformableNode(arFragment.transformationSystem)
        node.addChild(skeletonNode)
        node.setParent(anchorNode)

        arFragment.arSceneView.scene.addChild(anchorNode)

    }
}