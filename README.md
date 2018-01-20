# Jarkanoid
A Java/JavaFX based Arkanoid version

## Motivation and thoughts
I really like JavaFX - it's fun. Using Timelines, Animations, Property Binding and FXML makes it easy to build any kind of grafical user interface, even for games.

I tried to stick to the JavaFX flavor of the MVC pattern (powerful FMXL injected Controller which knows Model AND View) for this.

After being nearly feaure complete I'm not as convinced as before anymore that MVC is a good pattern for JavaFX for games with grafical "shapes". It would be much easier to use the JavaFX Shapes and their properties directly instead of replicating them in a model, e.g. JavaFX shapes have intersect() method which is quite convienient for collission detection. 

The better pattern would propably be a classical GameLoop pattern (GameLoop -> (processEvents, updateWorld, renderFrame). Although I do also have a game loop pattern in place it updates only model objects. The model then uses property bindung and the Observer pattern to update the view. 

The GameLoop pattern would also have made the code easier to understand and would probalby have avoided many line of code compared to the MVC pattern.
