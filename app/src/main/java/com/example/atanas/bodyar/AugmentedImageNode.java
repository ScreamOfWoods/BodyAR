package com.example.atanas.bodyar;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.TextView;

import com.google.ar.core.AugmentedImage;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Node for rendering an augmented image. The image is framed by placing the virtual picture frame
 * at the corners of the augmented image trackable.
 */

@SuppressWarnings({"AndroidApiChecker"})
public class AugmentedImageNode extends AnchorNode {

    private static final String TAG = "BodyAR_AIN";

    // The augmented image represented by this node.
    private AugmentedImage image;
    private ArFragment arFragment;
    private Context context;

    //private TextView organInfo;

    // Models of the 4 corners.  We use completable futures here to simplify
    // the error handling and asynchronous loading.  The loading is started with the
    // first construction of an instance, and then used when the image is set.
    private static CompletableFuture<ModelRenderable> liver;
    private static CompletableFuture<ModelRenderable> bronchus;
    private static CompletableFuture<ModelRenderable> heart;
    private static CompletableFuture<ModelRenderable> digestive;
    private static CompletableFuture<ModelRenderable> spine;
    private static CompletableFuture<ViewRenderable> liverInfoRender;
    private static CompletableFuture<ViewRenderable> bronchusInfoRender;
    private static CompletableFuture<ViewRenderable> heartInfoRender;
    private static CompletableFuture<ViewRenderable> digestiveInfoRender;
    private static CompletableFuture<ViewRenderable> spineInfoRender;

    public AugmentedImageNode(Context context, ArFragment arFragment) {
        this.arFragment = arFragment;
        this.context = context;

        //Load views
        liverInfoRender = buildView();
        bronchusInfoRender = buildView();
        heartInfoRender = buildView();
        spineInfoRender = buildView();
        digestiveInfoRender = buildView();

        // Upon construction, start loading the models.
        if(liver == null) {
            liver = buildModel("models/male_liver-lo.sfb");
            startRendering(liver);
        }

        if(bronchus == null) {
            bronchus = buildModel("models/male_bronchus-lo.sfb");
            startRendering(bronchus);
        }

        if(heart == null) {
            heart = buildModel("models/male_heart-lo.sfb");
            startRendering(heart);
        }

        if(digestive == null) {
            digestive = buildModel("models/digestive.sfb");
            startRendering(digestive);
        }

        if(spine ==null) {
            spine = buildModel("models/spine.sfb");
            startRendering(spine);
        }

    }

    /**
     * Called when the AugmentedImage is detected and should be rendered. A Sceneform node tree is
     * created based on an Anchor created from the image. The corners are then positioned based on the
     * extents of the image. There is no need to worry about world coordinates since everything is
     * relative to the center of the image, which is the parent node of the corners.
     */
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    public void setImage(AugmentedImage image) {
        if(image == null)
            return;

        this.image = image;
        Vector3 modelPosition = new Vector3(0.0f, 0.0f, 0.0f);

        // Set the anchor based on the center of the image.

        // If any of the models are not loaded, then recurse when all are loaded.
        switch(image.getName()) {
            case "liver":
                applyTransformation(liver, modelPosition,
                        applyTransformationView(liverInfoRender, new Vector3(0.0f, 0.25f, 0.0f),
                                R.string.liver_info));
                break;
            case "bronchus":
                applyTransformation(bronchus, modelPosition,
                        applyTransformationView(bronchusInfoRender, new Vector3(0.0f, 0.25f, 0.0f),
                                R.string.bronchus_info));
                break;
            case "heart":
                applyTransformation(heart, modelPosition,
                        applyTransformationView(heartInfoRender, new Vector3(0.0f, 0.25f, 0.0f),
                                R.string.heart_info));
                break;
            case "digestive":
                applyTransformation(digestive, modelPosition,
                        applyTransformationView(digestiveInfoRender, new Vector3(0.0f, 0.0f, 0.25f),
                                R.string.digestive_info));
                break;
            case "spine":
                applyTransformation(spine, modelPosition,
                        applyTransformationView(spineInfoRender, new Vector3(0.0f, 0.25f, 0.5f),
                                R.string.spine_info));
                break;

            default:
                Log.e(TAG, "Unrecognized image: " + image.getName());
                break;
        }
    }

    private Node applyTransformationView(CompletableFuture<ViewRenderable> renderable, Vector3 localPosition, int displayText) {
        startRenderingView(renderable);
        TextView organInfo = (TextView) renderable.getNow(null).getView();
        organInfo.setText(displayText);
        Node viewNode = new Node();
        viewNode.setLocalPosition(localPosition);
        viewNode.setRenderable(renderable.getNow(null));

        return viewNode;
    }

    private void applyTransformation(CompletableFuture<ModelRenderable> renderable, Vector3 localPosition, Node childView) {
        AnchorNode anchorNode = new AnchorNode(image.createAnchor(image.getCenterPose()));
        anchorNode.setParent(this);
        TransformableNode trNode = new TransformableNode(arFragment.getTransformationSystem());
        trNode.setParent(anchorNode);
        trNode.setLocalPosition(localPosition);
        trNode.addChild(childView);
        trNode.setRenderable(renderable.getNow(null));
        Node.OnTapListener baseListener = (hitTestResult, motionEvent) -> {
            TransformableNode tn = (TransformableNode) hitTestResult.getNode();
            tn.select();
            if(MainActivity.deleteFlag)
            {
                removeTrackable(trNode, childView, anchorNode);
            }
        };
        trNode.setOnTapListener(baseListener);
    }

    private void removeTrackable(TransformableNode nodeToBeDeleted, Node childDelete, AnchorNode anchorNode) {
        MainActivity.deleteFlag = false;
        nodeToBeDeleted.removeChild(childDelete);
        if(anchorNode.getAnchor() != null){
            anchorNode.getAnchor().detach();
        }
        this.removeChild(nodeToBeDeleted);
        //this.getAnchor().detach();
        this.setAnchor(null);
    }

    private void startRenderingView(@NotNull CompletableFuture<ViewRenderable> renderable) {
        if(!renderable.isDone()) {
            CompletableFuture.allOf(renderable)
                    .thenAccept((Void aVoid) -> setImage(image))
                    .exceptionally(
                            throwable -> {
                                Log.e(TAG, "Exception loading", throwable);
                                return null;
                            }
                    );
        }
    }

    private CompletableFuture<ViewRenderable> buildView() {
        return ViewRenderable.builder()
                .setView(context, R.layout.organ_information)
                .build();
    }

    private CompletableFuture<ModelRenderable> buildModel(String pathToModel){
        return ModelRenderable.builder()
                .setSource(context, Uri.parse(pathToModel))
                .build();
    }

    private void startRendering(CompletableFuture<ModelRenderable> renderable) {
        if(!renderable.isDone()) {
            CompletableFuture.allOf(renderable)
                    .thenAccept((Void aVoid) -> setImage(image))
                    .exceptionally(
                            throwable -> {
                                Log.e(TAG, "Exception loading", throwable);
                                return null;
                            }
                    );
        }
    }

    public AugmentedImage getImage() {
        return image;
    }
}