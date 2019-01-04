# TheWindAlbum
### 零、功能介绍
* 仿微信样式、交互
* 相册多选、单选：可以进行裁剪，支持圆形、矩形，比例调整，图片大于 300K 会进行压缩，返回最终的文件路径
* 视频多选、单选：返回文件路径
* 调用系统相机拍照：返回原图路径，拿到路径后可以进行裁剪
* 可修改主题颜色

项目开发中需要用到多选相册，网上没有找到合适的，所以自己写了一个。
已兼容 7.0，DEMO 中没有动态申请权限，运行 DEMO 拍照闪退的话，自行去打开拍照权限。
站在巨人的肩膀上，写起来也比较方便，项目中用到的第三方依赖有：
```
com.zhy:base-adapter
com.zhy:base-rvadapter
top.zibin:Luban
com.github.bumptech.glide
com.github.yalantis:ucrop
```
测试机型：
红米1S Android 4.4；
模拟器 Android 5.0；
小米5S Android 8.0
### 一、集成方式
#### 1、在项目的 build.gradle 添加如下代码
```
allprojects {
  repositories {
    maven { url 'https://jitpack.io' }
  }
}
```
#### 2、在 module 的 build.gradle 添加如下代码
```
implementation 'com.github.TheWindMeanFar:TheWindAlbum:1.1.2'
```
### 二、使用方式
如果不设置保存路径，默认路径在 SD 卡根目录的 TheWindAlbum 文件夹
#### 1、打开相机拍照

（1）使用默认配置
```
TheWind.get().with(MainActivity.this)
  .initValue()
  .openCamera(); // 打开相机拍照
```
（2）也可以自定义一些配置
```
TheWind.get().with(MainActivity.this)
  .initValue()
  .setCircle(true)  // 可选设置，裁剪时是否为圆形，默认为 false 矩形，如果拍照后不需要裁剪，可以不用设置
  .setCameraFileSavePath(savePathRoot + "camera_" + new Date().getTime() + ".jpg") // 可选设置，不设置的话会有默认路径
  .setCropFileSavePath(savePathRoot + "crop_" + new Date().getTime() + ".jpg") // 可选设置，不设置的话会有默认路径，，如果拍照后不需要裁剪，可以不用设置
  .setMaxHeight(1000) // 可选设置，设置截图后的图片最大高度，默认 0 表示不限制，如果拍照后不需要裁剪，可以不用设置
  .setMaxWidth(1000) // 可选设置，设置截图后的图片最大宽度，默认 0 表示不限制，如果拍照后不需要裁剪，可以不用设置
  .openCamera(); // 打开相机拍照
```
#### 2、打开相册选择图片

（1）使用默认配置
```
TheWind.get().with(MainActivity.this)
  .initValue()
  .openAlbum(); // 打开相册
```
（2）也可以自定义一些配置
```
TheWind.get().with(MainActivity.this)
  .initValue()
  .setCircle(true) // 可选设置，裁剪时是否为圆形，默认为 false 矩形，如果拍照后不需要裁剪，可以不用设置
  .setMaxSize(2) // 可选设置，设置最多能选几张图，默认 9 张
  .setMaxHeight(1000) // 可选设置，设置截图后的图片最大高度，默认 0 表示不限制
  .setMaxWidth(1000) // 可选设置，设置截图后的图片最大宽度，默认 0 表示不限制
  .setCompressFileSavePath(savePathRoot) // 可选设置，大于 300K 的图片会进行压缩，不设置的话会有默认路径
  .setCropFileSavePath(savePathRoot + "crop_" + new Date().getTime() + ".jpg") // 可选设置，裁剪后的保存路径，不设置的话会有默认路径，，如果拍照后不需要裁剪，可以不用设置
  .openAlbum(); // 打开相册
```
#### 3、打开视频选择
```
TheWind.get().with(MainActivity.this)
  .initValue()
  .setMaxSize(5) // 可选设置，设置最多能选几个视频，默认 9 个
  .openVideo();
```
### 三、接收选择结果
选择结果是在 onActivityResult 中接收的，代码如下，里面的「裁剪完成」是针对「调用拍照」完成后「调用裁剪」返回的。
如果是在相册中对图片编辑调用的裁剪，在 lib 中已经处理好了。
```
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // 视频选择完成
            case TheWind.VIDEO_REQUEST_CODE:
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> pathList = (ArrayList<String>) data.getSerializableExtra(TheWind.SELECT_RESULT_PATH_LIST);
                    tvPath.setText("视频选择完成：" + pathList.toString());
                }
                break;
                
            // 相册选择完成
            case TheWind.ALBUM_REQUEST_CODE:
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> pathList = (ArrayList<String>) data.getSerializableExtra(TheWind.SELECT_RESULT_PATH_LIST);
                    tvPath.setText("相册选择完成：" + pathList.toString());
                }
                break;
                
            // 拍照完成
            case TheWind.CAMERA_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    if (new File(TheWind.get().getCameraFileSavePath()).exists()) {
                        // 如果不裁剪，这就是拍照后的路径
                        // tvPath.setText("拍照成功：" + TheWind.get().getCameraFileSavePath());
                        // 如果要裁剪，注意裁剪不需要调用 initValue()
                        TheWind.get().with(MainActivity.this).openCrop();
                    } else tvPath.setText("拍照失败");
                }
                break;
                
            // 裁剪完成（拍照完成后调用裁剪触发的）
            case TheWind.CROP_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    if (new File(TheWind.get().getCropFileSavePath()).exists()) {
                        tvPath.setText("裁剪成功：" + TheWind.get().getCropFileSavePath());
                    } else
                        tvPath.setText("裁剪失败");
                }
                break;
        }
    }
```
### 四、样式修改
如果对默认的那种黑绿配色不满意，或者不符合自己的 APP 主题，可以修改主题颜色，只需在自己的项目的 colors.xml 中配置以下颜色
```
    <color name="colorTopBarWind">#FFFF7C00</color>
    <color name="colorBottomBarWind">#FFFF7C00</color>
    <color name="colorButtonWind">#FFFF7C00</color>
    <color name="colorBorderWind">#FFFF7C00</color>
    <color name="colorBackgroundWind">#f1f1f1</color>
```
还有就是 CheckBox 的颜色修改，只需将以下 4 张图修改颜色后添加到自己项目中即可，注意文件名称不能更改

ic_center_check_wind.png
![ic_center_check_wind.png](https://raw.githubusercontent.com/TheWindMeanFar/TheWindAlbum/master/albumlib/src/main/res/mipmap-xhdpi/ic_center_check_wind.png)

ic_center_uncheck_wind.png 因为是白色，所以看不到
![ic_center_uncheck_wind.png](https://raw.githubusercontent.com/TheWindMeanFar/TheWindAlbum/master/albumlib/src/main/res/mipmap-xhdpi/ic_center_uncheck_wind.png)

ic_check_wind.png
![ic_check_wind.png](https://raw.githubusercontent.com/TheWindMeanFar/TheWindAlbum/master/albumlib/src/main/res/mipmap-xhdpi/ic_check_wind.png)

ic_uncheck_wind.png 因为是白色，所以看不到
![ic_uncheck_wind.png](https://raw.githubusercontent.com/TheWindMeanFar/TheWindAlbum/master/albumlib/src/main/res/mipmap-xhdpi/ic_uncheck_wind.png)

