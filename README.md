# ActiveVocabe
Simple app to remember new foreign words. Project is in development still and there are minor bugs.

![ActiveVocabe](https://cloud.githubusercontent.com/assets/14358106/25313974/a859675c-2842-11e7-8961-d90588bef8be.png)

## HowTo

The main feature is possibility of auto-testing. After you press 'quiz' button
in the left down corner
app will ask you several questions to check your understanding.

<img src="https://cloud.githubusercontent.com/assets/14358106/25314033/530d4528-2844-11e7-82b5-050eccf0b2e2.png" width="248">

Actually, only exact answers considered correct. For the example above correct answers will be 'преследование' or 'стремление'
(any of them),
because only these two are in the table as translations.

After the quiz ended you will see your score and mistakes. You may repass quiz if you want to achieve better results.

<img src="https://cloud.githubusercontent.com/assets/14358106/25314080/5ab5cdbc-2845-11e7-90e1-09c8525884e8.png" width="348">

Several types of quizes are supported:

* **Random** will select words for test randomly from all sessions
* **Rating** will select firstly words in which you gave the least number of correct answers
* **Session** - only words from the chosen session will be added
* **Resent Errors** - only words in which you made mistakes during current app working session will be added

You should also set max number of words in the quiz and the form of quiz.

<img src="https://cloud.githubusercontent.com/assets/14358106/25314158/392d5c6c-2847-11e7-9d59-b3c5fb3860f1.png" width="248">

How to add words? Firstly you should create your first session where words will be located. 
You may also create several folders and sessions inside them to better organize your workflow.
After creating a session you can add words manually one by one entering word and it's translations separated by ';'.
Also you may upload list of words from file (press button 'Load' for this purpose).

<img src="https://cloud.githubusercontent.com/assets/14358106/25314210/4bd099be-2848-11e7-84e3-03e2a977b997.png" width="348">
<img src="https://cloud.githubusercontent.com/assets/14358106/25314297/f1791a16-2849-11e7-9f21-362a37514a21.png" width="348">

## How to build and run

To build you need at least Java 8 update 40 and Maven

1. Clone ActiveVocabe at any directory <code>git clone https://github.com/andreyzharkov/ActiveVocabe.git</code>
2. Build with maven at ActiveVocabe's root folder (where pom.xml is located) with <code>mvn package</code>
3. Run with <code>mvn exec:java -D exec.mainClass=ru.dron.activevocabe.App</code>
