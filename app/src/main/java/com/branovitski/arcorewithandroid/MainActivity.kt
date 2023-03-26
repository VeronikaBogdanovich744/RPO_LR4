package com.branovitski.arcorewithandroid

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.ar.core.Anchor
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var arFragment: ArFragment
    private var anchorNode: AnchorNode? = null

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arFragment = sceneform_fragment as ArFragment

        arFragment.arSceneView.scene.addOnUpdateListener {
            arFragment.onUpdate(it)
        }

        btn_clear.setOnClickListener {
            removeObject(anchorNode);
            showBtn(false);
        }

        arFragment.setOnTapArPlaneListener { hit, plane, motionEvent ->
            placeObject(arFragment, hit.createAnchor(), Uri.parse("andy.sfb"))
            showBtn(true);
        }

        showBtn(false);
    }

    private fun showBtn(bool: Boolean){
        if (bool) {
            btn_clear.isEnabled = true
            btn_clear.visibility = View.VISIBLE
        } else {
            btn_clear.isEnabled = false
            btn_clear.visibility = View.GONE
        }
    }

    private fun removeObject(anchorNode: AnchorNode?) {
        if (anchorNode != null) {
            arFragment.arSceneView.scene.removeChild(anchorNode)
            anchorNode.anchor?.detach()
            anchorNode.setParent(null)
        }
    }


    @RequiresApi(Build.VERSION_CODES.N)
    private fun placeObject(fragment: ArFragment, anchor: Anchor, model: Uri) {
        //загрузка модели
        ModelRenderable.builder()
            .setSource(fragment.context, model)
            .build()
            .thenApply {
                addNodeToScene(fragment, anchor, it)
            }
            .exceptionally {
                Toast.makeText(this, "Error", Toast.LENGTH_LONG).show()
                return@exceptionally
            }
    }

    private fun addNodeToScene(fragment: ArFragment, anchor: Anchor, renderable: ModelRenderable) {
        if(anchorNode!=null){
            removeObject(anchorNode)
        }
        anchorNode = AnchorNode(anchor)
        val transformableNode = TransformableNode(fragment.transformationSystem)
        transformableNode.renderable = renderable
        transformableNode.setParent(anchorNode) // прикрепление модели к якорю
        fragment.arSceneView.scene.addChild(anchorNode)
        transformableNode.select()
    }
}
