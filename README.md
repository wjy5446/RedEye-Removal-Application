# Remove-Red-Eye
> This application removes red-eyes using Adaboost and Redness





![alt text](https://github.com/wjy5446/Remove-Red-Eye/blob/master/image/abstract.png){: width="50%" height="50%"}





- Code

>C:\Users\magenta_jy\Desktop\Program\Remove-Red-Eye\app\src\main\java\com\example\vision_jy\redeyereomove2\
>
>- MainActivity.java
>- RedeyeFunction_jy.java





## 1. Overall method

![alt text](https://github.com/wjy5446/Remove-Red-Eye/blob/master/image/flow-chart.png){: width="50%" height="50%"}



## 2. Find face

![alt text](https://github.com/wjy5446/Remove-Red-Eye/blob/master/image/find-face.png){: width="50%" height="50%"}

- Face is found by the adaboost and haar-like feature using Open-CV







## 3. Redness 

![alt text](https://github.com/wjy5446/Remove-Red-Eye/blob/master/image/redness-equ.png){: width="50%" height="50%"}

![alt text](https://github.com/wjy5446/Remove-Red-Eye/blob/master/image/redness.png){: width="50%" height="50%"}

- Redness extracts the red pixels







## 4. Labeling

![alt text](https://github.com/wjy5446/Remove-Red-Eye/blob/master/image/labeling.png)

- The red region is labeled and remove noises







## 5. Find red-eyes

![alt text](https://github.com/wjy5446/Remove-Red-Eye/blob/master/image/detect-eyes.png){: width="50%" height="50%"}





### 5-1. Filter using single label

![alt text](https://github.com/wjy5446/Remove-Red-Eye/blob/master/image/find-eyes.png){: width="50%" height="50%"}





### 5-2. Filter using pair labels

![alt text](https://github.com/wjy5446/Remove-Red-Eye/blob/master/image/find-eyes2.png){: width="50%" height="50%"}





### 5-3. Additions

![alt text](https://github.com/wjy5446/Remove-Red-Eye/blob/master/image/another.png){: width="50%" height="50%"}

- Small red regions are dilated by lowering threshold around seed region.







## 6. Correct Red-eyes

![alt text](https://github.com/wjy5446/Remove-Red-Eye/blob/master/image/correct-equ.png){: width="50%" height="50%"}



## 7. Result

### 7-1. Experiment

![alt text](https://github.com/wjy5446/Remove-Red-Eye/blob/master/image/samples.png){: width="50%" height="50%"}



- I correct 80 samples for experiment.

![alt text](https://github.com/wjy5446/Remove-Red-Eye/blob/master/image/result.png){: width="50%" height="50%"}



- This algorithm is very bad, But i learn how to use openCV and android.





### 7-2. Mobile App

![alt text](https://github.com/wjy5446/Remove-Red-Eye/blob/master/image/UI.png){: width="50%" height="50%"}



- Correct Red-eyes

![alt text](https://github.com/wjy5446/Remove-Red-Eye/blob/master/image/abstract.png){: width="50%" height="50%"}



- Save iamges

![alt text](https://github.com/wjy5446/Remove-Red-Eye/blob/master/image/save_face.png){: width="50%" height="50%"}