### CameraUpload
<br>
An APK for testing this app is available [here](https://github.com/addiittya2006/CameraUpload/releases). 

Task List:<br>

- [x] Camera
    - [x] flash
    - [ ] zoom (partial)
- [x] CropView 
    - [x] Crop
    - [x] Zoom
- [x] Upload to Google Cloud Data Bucket
- [x] retrieve images from the Bucket and display
- [x] Image preview Activity (Full Image View)
- [x] Works on Landscape and Portrait (mostly)

This includes a basic custom camera implementation with flash and gives result to a cropping view which in turn uploads the cropped bitmap to a GCP datastore bucket.<br><br>
Note:<br>
Implementing full sized Image View with zooming is a little cumbersome so that functionality has been inspired from [here](https://github.com/dibakarece/OwnGalleryView/blob/master/app/src/main/java/org/ece/owngallery/ui/helpercomponent/GestureImageView.java). Also, the only third party lib has been used for the image crop view lies [here](https://github.com/ArthurHub/Android-Image-Cropper#using-view).