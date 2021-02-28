The solution works for up to the 5-th level.
On the 6-th one RAM consumption goes into space :(

To run save the map into the __resources/map.txt__ file
(cr/lf line ends, without the "map:" first line), the execute Application.main.
The program will output some log info and then save the list of rotations
into the __rotations.txt__ file in the current directory.

There is a small app in the index.html file (just open in a browser).
You can type command + enter in the "Command:".
Everything is logged in the console. The map in the page document is also clickable
(may be slow for L >= 4). I usually create a map with the browser app and then copy it into
the map.txt file (was too lazy to create a normal UI in the Java app itself :)).

