### Gradle
``` groovy
allprojects {
		repositories {
			...
			maven { url 'https://www.jitpack.io' }
		}
	}
```
``` groovy
	dependencies {
	        implementation 'com.github.yellowcath:MediaDecoder:1.4.1'
	}
```

### Usage
#### Original Video Frame
``` java
List<MediaData> dataList = new LinkedList<>();
        dataList.add(new MediaData("/sdcard/video1.mp4"));
        dataList.add(new MediaData("/sdcard/video2.mp4"));
        dataList.add(new MediaData("/sdcard/video3.mp4"));
        MultiMediaDecoder multiMediaDecoder = new MultiMediaDecoder(dataList);
        multiMediaDecoder.setOnFrameDecodeListener(new OnFrameDecodeListener() {
            @Override
            public void onFrameDecode(final Image frameImage, int codecColorFormat, final long frameTimeUs, boolean end) {
            }

            @Override
            public void onDecodeError(Throwable t) {
            }
        });
        try {
            multiMediaDecoder.setLoop(true);
            multiMediaDecoder.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
        multiMediaDecoder.start();

```

#### Yuv Frame
``` java
    public enum YuvType {
        YUV420P,
        YUV420SP,
        YUV
    }
```

``` java
    final VideoFrameAdapter videoFrameAdapter = new VideoFrameAdapter(VideoFrame.YuvType.YUV420SP);
    
    multiMediaDecoder.setOnFrameDecodeListener(new OnFrameDecodeListener() {
        @Override
        public void onFrameDecode(final Image frameImage, int codecColorFormat, final long frameTimeUs, boolean end) {
               VideoFrame videoFrame = videoFrameAdapter.adapte(frameImage, codecColorFormat, frameTimeUs);
        }

        @Override
        public void onDecodeError(Throwable t) {
        }
    });
``` 

#### MediaDataPool

``` java
    final VideoFrameAdapter videoFrameAdapter = new VideoFrameAdapter(VideoFrame.YuvType.YUV420SP);
    final MediaDataPool<VideoFrame> framePool = new MediaDataPool<>(10,10,videoFrameAdapter);
    multiMediaDecoder.setOnFrameDecodeListener(new OnFrameDecodeListener() {
        @Override
        public void onFrameDecode(final Image frameImage, int codecColorFormat, final long frameTimeUs, boolean end) {
               framePool.offer(frameImage,codecColorFormat,frameTimeUs);
        }

        @Override
        public void onDecodeError(Throwable t) {
        }
    });
    
    ...
    
    VideoFrame videoFrame = framePool.poll();
    ...
    //doFrame
    ...
    framePool.cacheObject(videoFrame);
```
