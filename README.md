---
## About
A framework that help u compress picture more easier.(Core is [libjpeg-turbo 2.0.2](https://github.com/libjpeg-turbo/libjpeg-turbo/releases/tag/2.0.2))

[中文文档](https://sharrychoo.github.io/blog/android/2019/06/17/Android-%E5%9B%BE%E7%89%87%E5%8E%8B%E7%BC%A9%E6%A1%86%E6%9E%B6-SCompressor.html)

---
## Current Version 
[![](https://jitpack.io/v/SharryChoo/SCompressor.svg)](https://jitpack.io/#SharryChoo/SCompressor)

---
## How to integration
### Step 1
Add it in your **root build.gradle** at the end of repositories
```
allprojects {
    repositories {
    	...
	    maven { url 'https://jitpack.io' }
    }
}
```

### Step 2
Add it in your **module build.gradle** at the end of dependencies
```
dependencies {
    ...
    implementation 'com.github.SharryChoo:SCompressor:x.x.x'
}
```

---
## How to use
### Initialize
Initialize at application create.
```
SCompressor.init(this);
```

### Setup input source
```
// 1. Support file path.
SCompressor.create()
       // set file path.
       .setInputPath(inputPath)
       ......
});

// 2. Support bitmap
SCompressor.create()
       // set origin bitmap.
       .setInputBitmap(originBitmap)
       ......
});

// 3. Support custom input source.
SCompressor.create()
       // custom input source
       .setInputSource(new DataSource<Object>() {
           @NonNull
           @Override
           public Class<Object> getType() {
                return null;
           }

           @Nullable
           @Override
           public Object getSource() {
               return null;
           }
       })
       ......
});
```
Use input file path can get nice efficiency.

If u custom input source, u need implement InputAdapter and add it.

### Setup options
```
SCompressor.create()
        .setInputPath(url)
        // range of 0 ~ 100.
        .setQuality(70)
        // set desire output size.
        .setDesireSize(500, 1000)
        // default is true: If u don't set desire size, it will auto down sample.
        .setAutoDownSample(true)
        ......
```

### Setup output source
```
// 1. Support config output path.
SCompressor.create()
        .setInputPath(inputPath)
        .setQuality(70)
        .setOutputPath(outputPath)
        ......
        
// 2. Support output bitmap
SCompressor.create()
        .setInputPath(inputPath)
        .setQuality(70)
        .asBitmap()
        ......
        
// 3. Support output byte array
SCompressor.create()
        .setInputPath(inputPath)
        .setQuality(70)
        .asByteArray()
        ......
        
// 3. Support custom output source.
SCompressor.create()
        .setInputPath(inputPath)
        .setQuality(70)
        // custom output source
        .setOutputSource(new DataSource<Object>() {
           @NonNull
           @Override
           public Class<Object> getType() {
                return null;
           }

           @Nullable
           @Override
           public Object getSource() {
               return null;
           }
       })
        ......        
```
The default output source is file path.

If u custom output source, u need implement OuputAdapter and add it.

### Asynchronous Call
```
// normal async call.
SCompressor.create()
        .setInputPath(inputPath)
        .setQuality(70)
        .asBitmap()
        .asyncCall(new CompressCallback<Bitmap>() {
            @Override
            public void onCompressSuccess(@NonNull Bitmap compressedData) {
                // ......
            }

            @Override
            public void onCompressFailed(@NonNull Throwable e) {
                // ......
            }
        });
        
// lambda async call.
SCompressor.create()
        .setInputPath(inputPath)
        .setQuality(70)
        .asBitmap()
        .asyncCall(new CompressCallbackLambda<Bitmap>() {
            @Override
            public void onCompressComplete(boolean isSuccess, @Nullable Bitmap compressedData) {
                 if (isSuccess) {
                      ......// if isSuccess, the compressedData non null.
                 }
            }
        });
```

### Synchronous call
```
Bitmap bitmap = SCompressor.create()
        .setInputPath(inputPath)
        .setQuality(70)
        .asBitmap()
        .syncCall()
```

### Other 
If u use custom input or output source, U need implement Adapter and add it.
#### InputAdapter
```
// Define Input Adapter
InputAdapter<Object> myInputAdapter = new InputAdapter<Object>() {
     @Override
     public String adapt(@NonNull Request request, @NonNull Object inputData) throws Throwable {
         // Request: u can fetch everything from request.
         // inputData: u need adapt this data 2 input file.
         return null;
     }

     @Override
     public boolean isAdapter(@NonNull Class adaptedType) {
          // Ensure this Adapter field.
          return Object.class.getName().equals(adaptedType.getName());
     }
};

// Add to scompressor.
SCompressor.addInputAdapter(myInputAdapter);
```

#### OutputAdapter
```
OutputAdapter<Object> myOutputAdapter = new OutputAdapter<Object>() {
     @Override
     public Object adapt(@NonNull File compressedFile) {
         // compressedFile: this file is compressed output file, u need adapt it to u desire obj.
         return null;
     }

     @Override
     public boolean isAdapter(@NonNull Class adaptedType) {
          // Ensure this Adapter field.
          return Object.class.getName().equals(adaptedType.getName());
     }
};

// Add to scompressor.
SCompressor.addOutputAdapter(myOutputAdapter);
```
---
## License
```
Copyright 2019 drakeet.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
