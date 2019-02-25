Dépot git pour le code de la bibliothèque MQTT-SN et les clients Arduino associés
=====================

URL publique : https://framagit.org/pierrick/mqtt-sn.git


# Installer la bibliothèque MQTT-SN pour l'IDE Arduino

Copier le dossier "arduino/library/" dans le dossier "libraries" de l'IDE ( ~/sketchbook/libraries ou ~/arduino/libraries ou dans le répertoire d'installation de l'IDE ).

	$> cp -R ./arduino/library ~/sketchbook/libraries

# Exemple d'utilisation de la bibliothèque

Un exemple d'utilisation de la bibliothèque se trouve dans le dossier "arduino/client-examples".
Copier ce dossier dans le répertoire d'exemples de l'IDE ( ~/sketchbook ou dans le répertoire "examples" du dossier d'installation de l'IDE ).

	$> cp -R ./arduino/client-examples/mqttsn/ ~/sketchbook

Vous avez ensuite directement accès à l'exemple depuis l'IDE : Fichier -> Carnet de croquis -> mqttsn

L'exemple utilise lecteur de tag RFID pour fonctionner. Il envoie sur le topic "TOPIC_PUB" l'identifiant du tag à chaque fois qu'il est approché du lecteur.
Dans l'exemple le programme est abonné au topic "TOPIC_SUB" et affiche régulièrement les messages reçus sur ce topic.
