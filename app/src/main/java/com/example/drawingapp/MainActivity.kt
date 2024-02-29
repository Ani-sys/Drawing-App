package com.example.drawingapp


import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.example.drawingapp.databinding.ActivityMainBinding
import com.bumptech.glide.request.transition.Transition
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream


class MainActivity : AppCompatActivity(){
    private lateinit var binding: ActivityMainBinding
    private var drawingView: DrawingView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        drawingView = binding.drawingView
        drawingView?.setSizeForBrush(20.toFloat())


        // setting OnClickListener for different colors
        binding.vBlue.setOnClickListener { selectColor(it) }
        binding.LightBlue.setOnClickListener { selectColor(it) }
        binding.Purple.setOnClickListener { selectColor(it) }
        binding.DarkPink.setOnClickListener { selectColor(it) }
        binding.vPink.setOnClickListener { selectColor(it) }
        binding.vYellow.setOnClickListener { selectColor(it) }
        binding.vGreen.setOnClickListener { selectColor(it) }
        binding.vRed.setOnClickListener { selectColor(it) }


        // getting different brush sizes
        binding.vBrush.setOnClickListener {
            showBrushChooserDialog()
            val drawable: Drawable? = ContextCompat.getDrawable(this, R.drawable.brush)
            drawable?.setColorFilter(ContextCompat.getColor(this, R.color.black), PorterDuff.Mode.SRC_IN)
            binding.vBrush.setImageDrawable(drawable)

        }

        // creating project title
        binding.tvDone.setOnClickListener {
            doneBtn()

        }

        // undo drawing
        binding.vUndo.setOnClickListener {
        drawingView?.onClickUndo()

        }

        // redo drawing
        binding.vRedo.setOnClickListener {
            drawingView?.onClickRedo()
        }

        // fill with the color
        binding.vImageShare.setOnClickListener {
         shareImage(result = "")
            val drawable: Drawable? = ContextCompat.getDrawable(this, R.drawable.share_icon)
            drawable?.setColorFilter(ContextCompat.getColor(this, R.color.black), PorterDuff.Mode.SRC_IN)
            binding.vImageShare.setImageDrawable(drawable)
        }

        // open color palette
        binding.vColors.setOnClickListener {
            showCustomColorPickerDialog()
        }

        // select an image from the gallery and using it as background
        binding.vImage.setOnClickListener {
            chooseImageFromGallery()
            val drawable: Drawable? = ContextCompat.getDrawable(this, R.drawable.image)
            drawable?.setColorFilter(ContextCompat.getColor(this, R.color.black), PorterDuff.Mode.SRC_IN)
            binding.vImage.setImageDrawable(drawable)
        }
    }

    private fun doneBtn(){
        binding.tvDone.setOnClickListener {
        val titleText = binding.title.text.toString()
            if (titleText.isEmpty()){
                binding.title.setHintTextColor(ContextCompat.getColor(this,R.color.edit_text_error_color))
            }else{
               binding.titleTextView.text = titleText
                binding.titleTextView.visibility = View.VISIBLE
                binding.title.visibility = View.GONE
            }
        }
    }

     private fun showBrushChooserDialog(){
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush size: ")
        // need to be fixed: !binding cannot find ibSmallBrush
        val smallBrushBtn = brushDialog.findViewById<ImageButton>(R.id.ib_small_brush)
        smallBrushBtn.setOnClickListener {
            drawingView?.setSizeForBrush(15.toFloat())
            brushDialog.dismiss()
        }
            brushDialog.show()

        val mediumBrushBtn = brushDialog.findViewById<ImageButton>(R.id.ib_medium_brush)
        mediumBrushBtn.setOnClickListener {
            drawingView?.setSizeForBrush(20.toFloat())
            brushDialog.dismiss()
        }
        brushDialog.show()

        val largeBrushBtn = brushDialog.findViewById<ImageButton>(R.id.ib_large_brush)
        largeBrushBtn.setOnClickListener {
            drawingView?.setSizeForBrush(30.toFloat())
            brushDialog.dismiss()
        }

        val extraLargeBrushBtn = brushDialog.findViewById<ImageButton>(R.id.ib_extra_large_brush)
         extraLargeBrushBtn.setOnClickListener {
             drawingView?.setSizeForBrush(40.toFloat())
             brushDialog.dismiss()
         }
    }


     fun selectColor(view: View){
         drawingView ?: return

         if (view == null){
             Toast.makeText(this, "View is null", Toast.LENGTH_SHORT).show()
             return
         }
         val background = view.background
         if (background is ColorDrawable){
             val color = background.color
             val hexColor = String.format("#%06X", 0xFFFFFF and color)
             drawingView?.setColor(hexColor)
         }else{
             Toast.makeText(this, "Background is not a ColorDrawable", Toast.LENGTH_SHORT).show()
         }
        val color = (view.background as ColorDrawable).color

     }


    private fun showCustomColorPickerDialog(){
        val dialogView = layoutInflater.inflate(R.layout.color_picker_dialog, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.show()

        val colorButtons = arrayOf(
            R.id.ibBlueColor,R.id.ibUsafaBlue, R.id.ibDarkCerulean, R.id.ibCatalinaBlue,
            R.id.ibLightBlue,R.id.ibMediumTurqoise,R.id.ibSkyBlue,R.id.ibLightSkyBlue,
            R.id.ibPurple,R.id.ibPalatinatePurple,R.id.ibCadmiumViolet,R.id.ibPearlyPurple,
            R.id.ibDarkPink,R.id.ibMagentaPink,R.id.ibPantonePink,R.id.ibChinaPink,
            R.id.ibTicklePink,R.id.ibPastelMagenta,R.id.ibLightHotPink,R.id.ibClassicRose,
            R.id.ibYellow,R.id.ibOrangeYellow,R.id.ibSunglow,R.id.ibDandelion,
            R.id.ibGreen,R.id.ibLimeGreen,R.id.ibSpringFrost,R.id.ibTeaGreen,
            R.id.ibRed,R.id.ibPigmentRed,R.id.ibFireRed,R.id.ibDarkRed
        )
        for (buttonId in colorButtons){
            val colorButton = dialogView.findViewById<ImageButton>(buttonId)
            colorButton.setOnClickListener {

                val colorDrawable = colorButton.background as? ColorDrawable
                val selectedColor = colorDrawable?.color ?: Color.BLACK

                Log.d("ColorPicker", "Selected Color: $selectedColor")

                val hexColors = String.format("#%06X", 0xFFFFFF and selectedColor)
                drawingView?.setColor(hexColors)
                dialog.dismiss()
            }
        }
    }


    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if (result.resultCode == Activity.RESULT_OK){
            val imageUri: Uri? = result.data?.data
            imageUri?.let {
                Glide.with(this)
                    .asBitmap()
                    .load(it)
                    .into(object : SimpleTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            drawingView?.setBackgroundBitmap(resource)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            super.onLoadCleared(placeholder)
                        }
                    })
            }
        }
    }

    private fun chooseImageFromGallery(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        getContent.launch(intent)
    }

    private fun shareImage(result: String){
        MediaScannerConnection.scanFile(this, arrayOf(result),null){
            path, uri ->
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            shareIntent.type = "image/png"
            startActivity(Intent.createChooser(shareIntent,"Share"))
        }
    }


}


