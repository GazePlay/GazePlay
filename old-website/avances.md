[Retour](../README.md) 

# Utilisateurs avancés

Cette page concerne uniquement les utilisateurs avancés, développeurs du logiciel en particulier.

## Fichier de configuration

Ce fichier sera bientôt généré à partir de l'interface.

Dans le répertoire par défaut de GazePlay (voir section ci-dessus), il faut créer un fichier texte dont le nom est GazePlay.properties s'il n'existe pas déjà.

Dans ce fichier, deux propriétés peuvent être activées. Si elles ne sont pas activées ou si la valeur indiquée n'existe pas, une valeur par défaut est utilisée.

1) La langue : LANGUAGE qui peut prendre 3 valeurs 

    1) fra pour français
    2) eng pour anglais
    3) deu pour allemand. 
    
    Par exemple, pour le jeu en anglais, la propriété doit être LANGUAGE = eng.
        
    Par défaut, la valeur est fra (français).
    
2) L'occulomètre : EyeTracker qui peut prendre 3 valeurs

    1) mouse_control : à choisir lorsque l'on utilise un eye-tracker capable de diriger la souris
    2) tobii_eyeX_4C : à choisir lorsque l'on utilise un Tobii 4C ou un Tobii EyeX
    3) eyetribe : à choisir lorsque l'on utilise un tracker de chez Eye-tribe

    Par exemple, pour un 4C ou un EyeX, mettre EyeTracker=tobii
    
[Retour](../README.md)