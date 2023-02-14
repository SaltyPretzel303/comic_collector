# Comic Collector

  Native Android application developed for the purpose of Mobile Systems and Services class at the Faculty of Electronic Engineering.
  
Description: 
  - Users can register or login with the existing credentials.
  - Users can create comics in the form of seris of images loaded from the gallery.
  - All created comics have to be placed somewhere on the (real world) map.
  - Users's movement is tracked and displayed on the map in real time.
  - By gettin close to already placed comic on the map, user can "collect" comic 
    which will then be added to his collections and available for later reading. 
  - If close enough, users can send each other "Friend request" in order to access 
    each others created and collected comics. 
    
  Data persistence, real time position synchronization and login is implemented using `Firebase`.  
  Map related functionalities are implemented using `OpenStreetMap`.
