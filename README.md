---
## About
A framework that help u compress picture more easier.(Core is [libjpeg-turbo 2.0.2](https://github.com/libjpeg-turbo/libjpeg-turbo/releases/tag/2.0.2))

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
### 1. Initialize
Initialize at application create.
```
SCompressor.init(this, authority);
```
- this: Application Context
- authority: help framework find URI for giving path.

### 2. Setup input source
```
// inject input data source
SCompressor.with(xxx)
       ......
```
Framework default support input source have: **Bitmap, String(file path), URI(file uri)**

If u pass other input data source, u need implement InputAdapter and invoke SCompressor.addInputAdapter. 

### 3. Setup options
```
SCompressor.with(xxx)
        // range of 0 ~ 100.
        .setQuality(70)
        // set desire output size.
        .setDesireSize(500, 1000)
        // default is true: If u don't set desire size, it will auto down sample.
        .setAutoDownSample(true)
        // if true will use arithmetic coding when compress jpeg
        // the compress ratio will higher 10% than Huffman 
        .setArithmeticCoding(true)
        // Set target length after compress to file.
        .setDesireLength(500 * 1024)
        // Set target file type
        .setCompressFormat(
             // Config without alpha channel
             CompressFormat.JPEG,
             // Config with alpha channel
             CompressFormat.WEBP
        )
        ......
```

### 4. Assign output type
```
SCompressor.with(xxx)
        // options
        ......
        // support output type
        .asBitmap()
        .asByteArray()
        .asUri()
        .asFilePath()
        .asFile()
        // Other outputSource
        .as(Class<?>)
        ......
```
SCompressor default support output type have: **Bitmap, byte[], Uri, String(file path), File** 

If u custom output source, u need implement OutputAdapter and invoke add it.

### 5. Call
#### 5.1 Asynchronous Call
```
// normal async call.
SCompressor.with(xxx)
        .setQuality(70)
        .asBitmap()
        .asyncCall(new CompressCallback<Bitmap>() {
            @Override
            public void onSuccess(@NonNull Bitmap compressedData) {
                // it will callback on UI Thread
            }

            @Override
            public void onFailed(@NonNull Throwable e) {
                // it will callback on UI Thread
            }
        });
        
// lambda async call.
SCompressor.with(xxx)
        .setQuality(70)
        .asBitmap()
        .asyncCall(new CompressCallbackLambda<Bitmap>() {
            @Override
            public void onCompressComplete(@NonNull Bitmap compressedData) {
                 // it will callback on UI Thread
            }
        });
```
#### 5.2 Synchronous call
```
Bitmap bitmap = SCompressor.with(xxx)
        .setInputPath(inputPath)
        .setQuality(70)
        .asBitmap()
        .syncCall()
```

### 6. Other 
If u use custom input or output source, U need implement Writer or Adapter and add it.

#### 6.1 InputAdapter
Adapter u assigned special input source to InputStream.
```
// Implementation Input Adapter
public class InputFileUriAdapter implements InputAdapter<Uri> {

    @Override
    public InputStream adapt(Context context, String authority, @NonNull InputSource<Uri> inputSource) throws Throwable {
        return context.getContentResolver().openInputStream(inputSource.getSource());
    }

    @Override
    public boolean isAdapter(@NonNull Class adaptedType) {
        return Uri.class.isAssignableFrom(adaptedType);
    }

}

// Add to scompressor.
SCompressor.addInputAdapter(new InputFileUriAdapter());
```

#### 6.2 OutputAdapter
Adapter compressed file to u desire outputType
```
// Implementation Output Adapter
public class OutputFilePathAdapter implements OutputAdapter<String> {
    @Override
    public String adapt(Context context, String authority, @NonNull File compressedFile) {
        return compressedFile.getAbsolutePath();
    }

    @Override
    public boolean isAdapter(@NonNull Class adaptedType) {
        return adaptedType.getName().equals(String.class.getName());
    }
}


// Add to scompressor.
SCompressor.addOutputAdapter(myOutputAdapter);
```
---

## License
```
Copyright 2019 SharryChoo.

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
