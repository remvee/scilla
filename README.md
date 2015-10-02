# Discontinued..

Feel free to use and modify this software. But beware, I am no longer
actively maintaining this code.

# Scilla - media server

Scilla answers media file requests and takes conversion parameters for
various media converters. Manipulated media files will be cached for
future requests. Scilla is designed to take requests using the servlet
API. a HttpServlet is included, it maps path-info to a file and passes
request parameters to converters.

## Features

* caching mechanism
* real-time; by following files being generated
* pluggable converters; currently implemented:

  * ImageMagick
  
    Allows conversions between various image types. Supports most of
    the options available for convert.

  * Lame
  
    Allows conversion from WAV to MP3 and recoding of MP3 to create
    streamable audio.
        
  * Ogg Vorbis
  
    Convert WAV to OGG and MP3 to OGG for higher quality audio
    streams. A bourne-shell script, depending on the vorbis-tools and
    mpg123, for converting MP3 to OGG and visa versa is included.
        
  * FFMpeg
  
    Allows conversions between various video types. Including scaling
    of picture size, framerate, video/ audio bitrate etc. Also the
    possibility to create a MP3 audio stream.

  * JAI
  
    Allows conversions between various images types. Currently only
    image scaling (with a ImageMagick like syntax) is supported.

# Copyright

Copyright 2001-2005 - R.W. van 't Veer
