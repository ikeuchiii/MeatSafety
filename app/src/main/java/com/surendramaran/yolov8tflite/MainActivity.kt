package com.surendramaran.yolov8tflite




import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import android.widget.ImageView
import com.bumptech.glide.Glide

import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.app.AppLaunchChecker
import androidx.core.content.ContextCompat
import com.surendramaran.yolov8tflite.Constants.LABELS_PATH
import com.surendramaran.yolov8tflite.Constants.MODEL_PATH
import com.surendramaran.yolov8tflite.databinding.ActivityMainBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity(), Detector.DetectorListener {
    private lateinit var binding: ActivityMainBinding
    private val isFrontCamera = false
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var detector: Detector? = null

    private lateinit var cameraExecutor: ExecutorService



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if(AppLaunchChecker.hasStartedFromLauncher(this)){



        } else {
            AlertDialog.Builder(this,R.style.DialogTheme) // FragmentではActivityを取得して生成
                .setTitle("MeatSafety同意事項")
                .setMessage("""第１条 利用者
１．本規約は、本サービスの提供を希望して本アプリを自己の端末にダウンロードした利用者（以下「利用者」）に適用されます。
２．利用者は、本アプリを自己の端末にダウンロードし、本サービスを利用することにより、本規約に同意したものとみなされます。
３．本アプリのダウンロード、及び本サービスの利用にかかる通信費用は、利用者が負担するものとします。

第２条 本アプリの権利
１．本アプリ中の表示、及び本アプリを構成するプログラム等に係る著作権、商標権等すべての知的財産権は、グリルの達人またはコンテンツ提供者に帰属します。
２．著作権等の知的財産権に関する問題が生じた場合、利用者は自己の費用と責任において、その問題を解決するとともに、グリルの達人に対して何らの迷惑又は損害等を与えてはなりません。

第３条 禁止行為
１．利用者は次の各号に該当する行為を行ってはならず、利用者が次の各号に該当する行為を行った場合、グリルの達人は、利用者に事前通知することなく本サービスの提供を停止します。
（１）本アプリを複製、修正、変更、改変、または翻案する行為
（２）本アプリを構成するプログラム（オブジェクトコード、ソースコード等全てを含みます）を複製し、または第三者に開示する行為
（３）本サービスの運営を妨げる行為、またはその恐れのある行為
（４）本アプリの内容を本サービス利用以外の目的に使用する行為
（５）他の利用者、第三者もしくはグリルの達人に損害、不利益を与える行為、またはその恐れのある行為
（６）公序良俗に反する行為、法令に違反する行為、またはその恐れのある行為
（７）本規約に違反する行為
（８）その他、グリルの達人が不適当と判断する行為
２．利用者の前項各号に該当する行為により、他の利用者、第三者もしくはグリルの達人に損害が生じた場合、J06チームは利用者の利用資格の停止または抹消とともに当該損害の賠償を請求することがあります。
３．グリルの達人が前項に基づき利用資格の停止または抹消をしたことにより、利用者が本サービスを利用できなくなり、これにより当該利用者または第三者に損害が発生したとしても、グリルの達人の故意または過失による債務不履行または不法行為による損害が発生した場合を除き、グリルの達人は一切の責任を負いません。また、利用資格の喪失後も当該利用者は、全ての法的責任を負わなければなりません。

第４条 免責
１．グリルの達人は、以下の事項に関し、その一切の責任を問いません。
（１）利用者が本サービスを利用することにより、損害を受けた場合
（２）利用者が本サービスを利用することにより、他の利用者または第三者に対して損害を与えた場合
（３）利用者が本サービスを利用できなかった場合、または本サービスの利用に関し、グリルの達人に責のない事由により損害を被った場合
２．グリルの達人は、本アプリがすべての利用者の端末に対応することを保証しません。
（１）推奨のOSのバージョンであっても、端末によりアプリが正常に動作しない場合があります。
（２）本アプリはスマートフォン用アプリとして開発しており、タブレットでの動作保証はありません。
（３）本アプリは日本国内からのみご利用いただけます。国外からの通信およびVPN等による海外経由の通信の場合、ご利用いただけません。
３．グリルの達人は、本サービスの内容および利用者が本アプリを通じて知り得る情報について、その完全性、正確性、確実性、有用性等に関して、いかなる責任も負いません。
４．本アプリに掲載されている情報、画像およびリンク等を利用することにより、利用者の機器等に損害が生じた場合、また、ウィルス感染した場合等について、グリルの達人の故意または過失による債務不履行または不法行為による損害が発生した場合を除き、グリルの達人はいかなる責任も負いません。

第５条 その他
１．グリルの達人は、利用者の事前の承諾を得ることなく、本アプリ及び本サービスの内容の全部または一部を変更することがあります。
２．グリルの達人は、ご利用者の利益に適合する場合や相当の事由があると認められる場合には、周知することにより、利用者の事前の承諾を得ることなく本規約を変更することがあります。なお、変更後の本規約は、公表等告知の際に定める適用開始日から効力を生じることと致します。
３．グリルの達人が提供する他のアプリケーションソフトのサービスに関し別途使用条件等を提示したときに、当該使用条件等の規定が本規約と矛盾する場合には、当該使用条件等の規定が優先して適用されます。
４．利用者は、本サービスを利用するにあたって、本規約等以外に、対応端末の製造者や通信会社等の第三者が提供するサービスを併せて使用する場合には、当該第三者の規約等が適用される場合があります。""")
                .setPositiveButton("同意する", { dialog, which ->
                    // TODO:Yesが押された時の挙動
                    AppLaunchChecker.onActivityCreate(this);
                })
                .setNegativeButton("同意しない", { dialog, which ->
                    // TODO:Noが押された時の挙動
                    finishAndRemoveTask()
                })
                .show()
        }
        val help_btn = findViewById<ImageButton>(R.id.help_btn)
        help_btn.setOnClickListener {
            val intent = Intent(this, HelpActivity::class.java)
            startActivity(intent)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        cameraExecutor.execute {
            detector = Detector(baseContext, MODEL_PATH, LABELS_PATH, this)
        }
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        val gifImageView: ImageView = findViewById(R.id.imageView3)

        playGifEveryFiveSeconds(gifImageView, R.drawable.ledsb_result)

    }



    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider  = cameraProviderFuture.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider ?: throw IllegalStateException("Camera initialization failed.")

        val rotation = binding.viewFinder.display.rotation

        val cameraSelector = CameraSelector
            .Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        preview =  Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(rotation)
            .build()

        imageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetRotation(binding.viewFinder.display.rotation)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()

        imageAnalyzer?.setAnalyzer(cameraExecutor) { imageProxy ->
            val bitmapBuffer =
                Bitmap.createBitmap(
                    imageProxy.width,
                    imageProxy.height,
                    Bitmap.Config.ARGB_8888
                )
            imageProxy.use { bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer) }
            imageProxy.close()

            val matrix = Matrix().apply {
                postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())

                if (isFrontCamera) {
                    postScale(
                        -1f,
                        1f,
                        imageProxy.width.toFloat(),
                        imageProxy.height.toFloat()
                    )
                }
            }

            val rotatedBitmap = Bitmap.createBitmap(
                bitmapBuffer, 0, 0, bitmapBuffer.width, bitmapBuffer.height,
                matrix, true
            )

            detector?.detect(rotatedBitmap)
        }

        cameraProvider.unbindAll()

        try {
            camera = cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageAnalyzer
            )

            preview?.setSurfaceProvider(binding.viewFinder.surfaceProvider)
        } catch(exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) {
        if (it[Manifest.permission.CAMERA] == true) { startCamera() }
    }

    override fun onDestroy() {
        super.onDestroy()
        detector?.close()
        cameraExecutor.shutdown()
    }

    override fun onResume() {
        super.onResume()
        if (allPermissionsGranted()){
            startCamera()
        } else {
            requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
        }
    }

    companion object {
        private const val TAG = "Camera"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = mutableListOf (
            Manifest.permission.CAMERA
        ).toTypedArray()
    }

    override fun onEmptyDetect() {
        runOnUiThread {
            binding.overlay.clear()
        }
    }

    override fun onDetect(boundingBoxes: List<BoundingBox>, inferenceTime: Long) {
        runOnUiThread {

            binding.overlay.apply {
                setResults(boundingBoxes)
                invalidate()
            }
        }
    }

    private fun playGifEveryFiveSeconds(imageView: ImageView, gifResId: Int) {
        val handler = Handler(Looper.getMainLooper())
        val gifImageView: ImageView = findViewById(R.id.imageView3)
        val gifRunnable = object : Runnable {
            override fun run() {
                // GlideでGIFを表示
                Glide.with(imageView.context)
                    .asGif() // GIFとして読み込む
                    .load(R.drawable.ledsb_result) // res/drawable/sample_gif.gif
                    .into(gifImageView)

                // 3秒後にGIFを停止
                handler.postDelayed({
                    Glide.with(imageView.context).clear(imageView) // GIFを停止
                }, 10000) // 再生時間（3秒）

                // 再び5秒後にGIFを再生
                handler.postDelayed(this, 30000) // 次の再生タイミング
            }
        }

        // GIF再生開始
        handler.post(gifRunnable)
    }
}


