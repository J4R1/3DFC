# 3DFC
<!-- PROJECT LOGO -->
<br />
<p align="center">
  <a href="https://github.com/J4R1/3DFC">
    <img src="images/logo.png" alt="Logo" width="200" height="200">
  </a>

  <h3 align="center">3D Fauna center</h3>

  <p align="center">
    A virtual museum companion
    <br />
  </p>
</p>

<!-- markdown-toc start - Don't edit this section. Run M-x markdown-toc-refresh-toc -->
**Table of Contents**

- [About The Project](#about-the-project)
    - [Built with](#built-with)
- [Installation & Requirements](#installation--requirements)
    - [Requirements](#requirements)
    - [Installation](#installation)
- [Usage](#usage)
- [Licenses](#licenses)
    - [License for the models used](#license-for-the-models-used)
- [Authors](#authors)
- [Acknowledgements](#acknowledgements)

<!-- markdown-toc end -->

# About The Project 
<!-- Screenshot here  -->
<!-- Description here -->
3DFC is an app to transform your surrounding area into a natural history museum, or to add a dash of technology to an already existing one.

It uses AR-technology to display life size animals right next to you, with a small information table above them.

It also has the ability to recognize your current location, so that multiple museums can have differing models.
## Built with

  * Kotlin
  * Android Studio
  * Accessibility in mind: the colors we have used for the text fields are all vision-impaired friendly and all the buttons have alt-texts for screen readers. There is even a button to read the current animals info text out loud.
# Installation & Requirements

## Requirements
  * Minimum SDK version 26 (Android 8 Oreo)
  * Internet access  
    * At this point the animal info only works in Metropolia's networks
  * ARCore compatible phone 
  * Access to location services and camera

## Installation
  1. Download the project
  2. Run through Android Studio  
  
  Or  
 
  1. Download the APK
  2. Sideload into your Android phone
  3. Run

# Usage
<!-- Couple screenshots here  -->
We envisioned 3D Fauna Center to either be a replacement for a live museum experience, used in for example remote areas with little access to such sites or as a easy to implement and modify addition to traditional museums. 

That will explain itself briefly on startup, but here are the main points.

  1. Scan the area around you until a white dot matrix appears.
  2. Choose and animal from the bottom of the screen, it can be rotated using the two buttons at the bottom of the screen.
  3. To hear the info text read out loud, press the image of the speaker, above the right rotation button.
  4. (The button above that is the voice command button, which currently forces an ARCore reset.)
  5. The button on the top left shows a timestamped list of the places you have visited.

# Licenses
## License for the models used

MIT License

Copyright (c) 2019 bensadiku

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.


# Authors
[Jari Pietikäinen](https://www.github.com/J4R1) and [Oskari Sieranen](https://www.github.com/oskarits)

# Acknowledgements
* TX00CK66-3007
