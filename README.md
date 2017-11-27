# HighlightButton
a view for Android  that will perform animation when clicked

Use
===
step one
--------
&emsp;Put the customized style sheet in your res/values/attrs.xml<br>
```xml
<declare-styleable name="HighlightButtonStyle">
   <attr name="highlight_button_img_id" format="reference" />
   <attr name="highlight_button_height_self_adaption" format="boolean" />
   <attr name="highlight_button_width_self_adaption" format="boolean" />
   <attr name="highlight_button_max_animation_ratio" format="float" />
   <attr name="highlight_button_outline_color" format="color" />
</declare-styleable>
```
step two
--------
&emsp;Put HighlightButton view in your layout.<br>
&emsp;The parent view group must be choosen among LinearLayout , RelativeLayout and FrameLayout.<br>
&emsp;Here is an example below:<br>
```xml
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:custom="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/activity_main"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context="com.liuyuzhou.highlightanimationbutton.MainActivity">

    <com.liuyuzhou.highlightanimationbutton.HighlightButton
        android:id="@+id/button"
        android:layout_width="150dp" android:layout_height="200dp"
        android:layout_centerInParent="true"
        custom:highlight_button_img_id="@mipmap/choose" custom:highlight_button_outline_color="#787878"  />

</RelativeLayout>
```
&emsp;You should pay attention that since we use customized attribute , we shoud add **xmlns:custom="http://schemas.android.com/apk/res-auto"** to parent view.

step three
-----------
&emsp;In activity,get the view and customize its function.

Customized Attributes Information
=====================

highlight_button_img_id
-----------------------
&emsp;This attribute is must-set.It's the image that display your view.

highlight_button_height_self_adaption
-----------------------
&emsp;The format is boolean.If it is set true,it means you should assign your view a exact width , and height will depends on the image's size ratio and your view's width.<br>
&emsp;When using this view , you should set only one attribute between **highlight_button_height_self_adaption** and **highlight_button_width_self_adaption** to be true .If user set neither of them , **highlight_button_height_self_adaption** will be set true in default situation.

highlight_button_width_self_adaption
-----------------------
&emsp;The format is boolean.If it is set true,it means you should assign your view a exact height , and width will depends on the image's size ratio and your view's height.<br>
&emsp;When using this view , you should set only one attribute between **highlight_button_height_self_adaption** and **highlight_button_width_self_adaption** to be true .If user set neither of them , **highlight_button_height_self_adaption** will be set true in default situation.

highlight_button_max_animation_ratio
-----------------------
&emsp;The format is float.It determins the max size of the outline when animating. This value must be bigger than 1.

highlight_button_outline_color
-----------------------
&emsp;The format is color.It determins the color of the outline when animating.

Notice
======
&emsp;1.When Using this view , user must set its minSdk>=16.<br>
&emsp;2.HighlightButton uses ValueAnimator , so user can pull in **nineoldandroids** library to be compatible for the version before Android 11.

End
======
&emsp;Thanks for reading.If you find any problem , please leave a message to you . I will really appreciate your contribution.








