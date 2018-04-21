# Remove-Red-Eye
> This application removes red-eyes using Adaboost and Redness



![alt text](https://github.com/wjy5446/Remove-Red-Eye/blob/master/image/abstract.png)



## 1. Overall method

![alt text](https://github.com/wjy5446/Remove-Red-Eye/blob/master/image/flow-chart.png)



## 2. Find face

![alt text](https://github.com/wjy5446/Remove-Red-Eye/blob/master/image/find-face.png)

- Face is found by the adaboost and haar-like feature using Open-CV



## 3. Redness 

![alt text](https://github.com/wjy5446/Remove-Red-Eye/blob/master/image/redness-equ.png)

![alt text](https://github.com/wjy5446/Remove-Red-Eye/blob/master/image/redness.png)

- Redness extracts the red pixels



## 4. Labeling

![alt text](https://github.com/wjy5446/Remove-Red-Eye/blob/master/image/labeling.png)

- The red region is labeled and remove noises



## 5. Find red-eyes

![alt text](https://github.com/wjy5446/Remove-Red-Eye/blob/master/image/detect-eyes.png)

### 5-1. Filter using single label

![alt text](https://github.com/wjy5446/Remove-Red-Eye/blob/master/image/find-eyes.png)

### 5-2. Filter using pair labels

![alt text](https://github.com/wjy5446/Remove-Red-Eye/blob/master/image/find-eyes2.png)

### 5-3. Additions

![alt text](https://github.com/wjy5446/Remove-Red-Eye/blob/master/image/another.png)

- Small red regions are dilated by lowering threshold around seed region.



## 6. Correct Red-eyes

![alt text](https://github.com/wjy5446/Remove-Red-Eye/blob/master/image/correct-equ.png)



## 7. Result

### 7-1. Experiment

![alt text](https://github.com/wjy5446/Remove-Red-Eye/blob/master/image/samples.png)

- I correct 80 samples for experiment.

![alt text](https://github.com/wjy5446/Remove-Red-Eye/blob/master/image/result.png)

- This algorithm is very bad, But i learn how to use openCV and android.



### 7-2. Mobile App

![alt text](https://github.com/wjy5446/Remove-Red-Eye/blob/master/image/UI.png)

![alt text](https://github.com/wjy5446/Remove-Red-Eye/blob/master/image/abstract.png)

![alt text](https://github.com/wjy5446/Remove-Red-Eye/blob/master/image/save_face.png)