package com.example.atanas.bodyar;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import com.google.ar.core.AugmentedImage;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
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

    // Models of the 4 corners.  We use completable futures here to simplify
    // the error handling and asynchronous loading.  The loading is started with the
    // first construction of an instance, and then used when the image is set.
    private static CompletableFuture<ModelRenderable> liver;

    private static CompletableFuture<ModelRenderable> ulCorner;
    private static CompletableFuture<ModelRenderable> urCorner;
    private static CompletableFuture<ModelRenderable> lrCorner;
    private static CompletableFuture<ModelRenderable> llCorner;

    public AugmentedImageNode(Context context) {
        // Upon construction, start loading the models for the corners of the frame.
        if(liver== null) {
            liver =
                    ModelRenderable.builder()
                            .setSource(context, Uri.parse("models/male_liver-lo.sfb"))
                            .build();
        }
        /*if (ulCorner == null) {
            ulCorner =
                    ModelRenderable.builder()
                            .setSource(context, Uri.parse("models/frame_upper_left.sfb"))
                            .build();
            urCorner =
                    ModelRenderable.builder()
                            .setSource(context, Uri.parse("models/frame_upper_right.sfb"))
                            .build();
            llCorner =
                    ModelRenderable.builder()
                            .setSource(context, Uri.parse("models/frame_lower_left.sfb"))
                            .build();
            lrCorner =
                    ModelRenderable.builder()
                            .setSource(context, Uri.parse("models/frame_lower_right.sfb"))
                            .build();
        }*/
    }

    /**
     * Called when the AugmentedImage is detected and should be rendered. A Sceneform node tree is
     * created based on an Anchor created from the image. The corners are then positioned based on the
     * extents of the image. There is no need to worry about world coordinates since everything is
     * relative to the center of the image, which is the parent node of the corners.
     */
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    public void setImage(AugmentedImage image) {
        this.image = image;

        // If any of the models are not loaded, then recurse when all are loaded.
        if(!liver.isDone()){
            CompletableFuture.allOf(liver)
                    .thenAccept((Void aVoid) -> setImage(image))
                    .exceptionally(
                            throwable -> {
                                Log.e(TAG, "Exception loading", throwable);
                                return null;
                            });
        }

        /*if (!ulCorner.isDone() || !urCorner.isDone() || !llCorner.isDone() || !lrCorner.isDone()) {
            CompletableFuture.allOf(ulCorner, urCorner, llCorner, lrCorner)
                    .thenAccept((Void aVoid) -> setImage(image))
                    .exceptionally(
                            throwable -> {
                                Log.e(TAG, "Exception loading", throwable);
                                return null;
                            });
        }*/

        // Set the anchor based on the center of the image.
        setAnchor(image.createAnchor(image.getCenterPose()));

        // Make the 4 corner nodes.
        Vector3 localPosition = new Vector3();
        Node cornerNode;

        localPosition.set(0.0f, 0.0f, 0.0f);
        cornerNode = new Node();
        cornerNode.setParent(this);
        cornerNode.setLocalPosition(localPosition);
        cornerNode.setRenderable(liver.getNow(null));


        /*
        // Upper left corner.
        localPosition.set(-0.5f * image.getExtentX(), 0.0f, -0.5f * image.getExtentZ());
        cornerNode = new Node();
        cornerNode.setParent(this);
        cornerNode.setLocalPosition(localPosition);
        cornerNode.setRenderable(ulCorner.getNow(null));

        // Upper right corner.
        localPosition.set(0.5f * image.getExtentX(), 0.0f, -0.5f * image.getExtentZ());
        cornerNode = new Node();
        cornerNode.setParent(this);
        cornerNode.setLocalPosition(localPosition);
        cornerNode.setRenderable(urCorner.getNow(null));

        // Lower right corner.
        localPosition.set(0.5f * image.getExtentX(), 0.0f, 0.5f * image.getExtentZ());
        cornerNode = new Node();
        cornerNode.setParent(this);
        cornerNode.setLocalPosition(localPosition);
        cornerNode.setRenderable(lrCorner.getNow(null));

        // Lower left corner.
        localPosition.set(-0.5f * image.getExtentX(), 0.0f, 0.5f * image.getExtentZ());
        cornerNode = new Node();
        cornerNode.setParent(this);
        cornerNode.setLocalPosition(localPosition);
        cornerNode.setRenderable(llCorner.getNow(null));
        */
    }

    public AugmentedImage getImage() {
        return image;
    }
}
