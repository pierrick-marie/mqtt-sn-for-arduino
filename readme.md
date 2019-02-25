Dépot git pour le code de la bibliothèque MQTT-SN et les clients Arduino associés
=====================

URL publique : https://framagit.org/pierrick/mqtt-sn.git


# Installer la bibliothèque MQTT-SN pour l'IDE Arduino

Copier le dossier "arduino/library/mqttsn" dans le dossier "libraries" de l'IDE ( ~/Arduino/libraries ).

	$> cp -R ./arduino/library/mqttsn ~/Arduino/libraries

# Exemple d'utilisation de la bibliothèque

Un exemple d'utilisation de la bibliothèque se trouve dans le dossier "arduino/client-examples/mqttsn".
Copier ce dossier dans le répertoire d'exemples de l'IDE ( ~/Arduino/examples ).

	$> cp -R ./arduino/client-examples/mqttsn/ ~/Arduino/examples

Vous avez ensuite directement accès à l'exemple depuis l'IDE : Fichier -> Carnet de croquis -> mqttsn

L'exemple utilise lecteur de tag RFID pour fonctionner. Il envoie sur le topic "TOPIC_PUB" l'identifiant du tag à chaque fois qu'il est approché du lecteur.
Dans l'exemple le programme est abonné au topic "TOPIC_SUB" et affiche régulièrement les messages reçus sur ce topic.
