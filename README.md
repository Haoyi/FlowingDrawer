# FlowingDrawer 

基于“https://github.com/mxn21/FlowingDrawer” 做了更新，使用了AndroidX，而且增加了从上下滑出的效果。
由于自己项目中需要使用到，所以修改了包名。

![Showcase](/screen.gif)

swipe four directions to display drawer with flowing effects.


## Download

Include the following dependency in your build.gradle file.

Gradle:

```Gradle
    repositories {
        jcenter()
    }

    dependencies {
        implementation 'com.xiaoyan:flowingdrawer-core:1.0.0'
        implementation 'com.nineoldandroids:library:2.4.0'
    }
```

## V2.0.0 Features

 * The menu can be positioned along two edges:left and right .
 * Allows the drawer to be opened by dragging the edge or the entire screen.


## Usage

*For a working implementation of this project see the `app/` folder and check out the sample app*

activity_main.xml:

```xml
    <com.xiaoyan.flowingdrawer_core.FlowingDrawer
            xmlns:app="http://schemas.android.com/apk/res-auto"
            xmlns:android="http://schemas.android.com/apk/res/android"
            android:id="@+id/drawerlayout"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:clipChildren="false"
            android:clipToPadding="false"
            app:edPosition="1"
            app:edMenuSize="260dp"
            app:edMenuBackground="#dddddd">

        <!--content-->
        <RelativeLayout
                android:id="@+id/content"
                android:layout_width="match_parent"
                android:layout_height="match_parent"/>

        <!--menu-->
        <com.xiaoyan.flowingdrawer_core.FlowingMenuLayout
                android:id="@+id/menulayout"
                android:layout_width="match_parent"
                android:layout_height="match_parent">

            <FrameLayout
                    android:id="@+id/id_container_menu"
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"/>

        </com.xiaoyan.flowingdrawer_core.FlowingMenuLayout>

    </com.xiaoyan.flowingdrawer_core.FlowingDrawer>
```

To use a FlowingDrawer, position FlowingDrawer as the root , position your primary content view as the
first child with width and height of match_parent . Add FlowingMenuLayout as child views after the main
content view . FlowingMenuLayout commonly use match_parent for height and width.

Don't set any background on FlowingMenuLayout or FlowingMenuLayout's children, it means their background
should be transparent.

Don't set FlowingMenuLayout's width with a fixed width, it's not a useful way to change it's width .

You can change menu's attribute in FlowingDrawer layout node use custom attribute,like edMenuBackground,edMenuSize,
edPosition.

Use edPosition attribute corresponding to which side of the view you want the drawer
to emerge from: left or right.Left menu : edPosition =1 ; Right menu: edPosition =2 .

For more custom attribute ,you can see in attrs.xml.


MainActivity:

```java
     mDrawer = (FlowingDrawer) findViewById(R.id.drawerlayout);
     mDrawer.setTouchMode(ElasticDrawer.TOUCH_MODE_BEZEL);
     mDrawer.setOnDrawerStateChangeListener(new ElasticDrawer.OnDrawerStateChangeListener() {
                 @Override
                 public void onDrawerStateChange(int oldState, int newState) {
                     if (newState == ElasticDrawer.STATE_CLOSED) {
                         Log.i("MainActivity", "Drawer STATE_CLOSED");
                     }
                 }

                 @Override
                 public void onDrawerSlide(float openRatio, int offsetPixels) {
                     Log.i("MainActivity", "openRatio=" + openRatio + " ,offsetPixels=" + offsetPixels);
                 }
             });
```
setTouchMode can allows the drawer to be opened by dragging the edge or the entire screen.
setOnDrawerStateChangeListener can be used to monitor the state and motion of drawer views.
Avoid performing expensive operations such as layout during animation as it can cause stuttering.
ElasticDrawer.OnDrawerStateChangeListener offers default/no-op implementations of each callback method.


License
=======

    Copyright 2015 soul.mxn

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

