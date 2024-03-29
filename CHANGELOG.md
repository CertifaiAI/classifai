# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

The detailed rules and walkthrough of writing a changelog is located [here](https://docs.google.com/document/d/10N5B6ojby5rS7hq3cs83vqHfZgkHBU1HCKalsV2tOo0/edit#heading=h.uh2vc5aigamo)

*Note: This changelog is implemented since `2.0.0-alpha` version. All records before it are drafted based on [release notes](https://github.com/CertifaiAI/classifai/releases) might not be as detailed.*
## [2.0.0-alpha3] - 2021-12-14
### Added
- Added tutorial modal for user guide [#212](https://github.com/CertifaiAI/Classifai_FrontEnd/pull/212)
- Project versioning
- Database migration
- Project statistics [#500](https://github.com/CertifaiAI/classifai/pull/500)
- Add images to project [#519](https://github.com/CertifaiAI/classifai/pull/519)
- Different color for labels [#228](https://github.com/CertifaiAI/Classifai_FrontEnd/pull/228)
- Add label at the drop-down box, remove label still selected status,shortcut key to toggle annotation tool, zoom tool and revert default [224](https://github.com/CertifaiAI/Classifai_FrontEnd/pull/224)
- Shortcut key to shift canvas in zoom mode [#226](https://github.com/CertifaiAI/Classifai_FrontEnd/pull/226)
- Toggle tab by clicking icon [#216](https://github.com/CertifaiAI/Classifai_FrontEnd/pull/216)
- Updated keyShortcut.md
- Update tutorial list [#229](https://github.com/CertifaiAI/Classifai_FrontEnd/pull/229) [#215](https://github.com/CertifaiAI/Classifai_FrontEnd/pull/215)

### Fixed
- Project loading speed [#506](https://github.com/CertifaiAI/classifai/pull/506)
- Coco Json area, width and height [#227](https://github.com/CertifaiAI/Classifai_FrontEnd/pull/227)
- Delete previous point using backspace [#206](https://github.com/CertifaiAI/Classifai_FrontEnd/pull/206)
- Prevent icon enlarge when zoom in [#223](https://github.com/CertifaiAI/Classifai_FrontEnd/pull/223)

### Changed
- Refactored backend -> Implement Model, View, Controller design pattern
- Remove event bus for database query [#504](https://github.com/CertifaiAI/classifai/pull/504)
- Add database facade [#502](https://github.com/CertifaiAI/classifai/pull/502)
- JAX-RS implementation for API annotation [#507](https://github.com/CertifaiAI/classifai/pull/507)
- Remove Json Object -> using data transfer object for type checking [#505](https://github.com/CertifaiAI/classifai/pull/505)
- Add CORS enable [#511](https://github.com/CertifaiAI/classifai/pull/511)
- Remove wasabi service [#523](https://github.com/CertifaiAI/classifai/pull/523)
- Remove global state [#509](https://github.com/CertifaiAI/classifai/pull/509)
- Remove static object [#508](https://github.com/CertifaiAI/classifai/pull/508)
- Refactor database response [#499](https://github.com/CertifaiAI/classifai/pull/499)
- API terminate Classifai [#496](https://github.com/CertifaiAI/classifai/pull/496)

## [2.0.0-alpha2] - 2021-08-12
### Added
- Sorting of project card with options of created date, last modified date or project name.
- Renaming data from workspace - UI and F2 shortcut key
- Updated keyShortcut.md [#455](https://github.com/CertifaiAI/classifai/pull/455)
- Deleting data from workspace [#392](https://github.com/CertifaiAI/classifai/issues/392)
- Add guiding cross line for more effective bounding box labelling [#201](https://github.com/CertifaiAI/Classifai_FrontEnd/pull/201)
- Ask user confirmation when user want to delete a project [#201](https://github.com/CertifaiAI/Classifai_FrontEnd/pull/201)

### Fixed
- Fixed the display of full image path instead of intentional image name in Ubuntu and Mac OS [#446](https://github.com/CertifaiAI/classifai/issues/446)
- Bug fix for removing "\r" and spaces " " in imported labels [#437](https://github.com/CertifaiAI/classifai/issues/437)
- Support WebP image format [#394](https://github.com/CertifaiAI/classifai/issues/394)
- Fix create empty project [#466](https://github.com/CertifaiAI/classifai/issues/466)
- Frontend code refactoring [#198](https://github.com/CertifaiAI/Classifai_FrontEnd/pull/198)
- Fix images loaded inconsistent with/more than total images in a project folder [#487](https://github.com/CertifaiAI/classifai/issues/487)
- Fix polygon line not pointed to the cursor during drawing [#208](https://github.com/CertifaiAI/Classifai_FrontEnd/pull/208)
- Fix Undo/Redo when drawing polygons [#205](https://github.com/CertifaiAI/Classifai_FrontEnd/pull/205)

## [2.0.0-alpha1] - 2021-02-26
### Added
- Label filtering feature
- Added CodeofConduct.md and Contributing.md 
- Changed create project workflow  
  to insert project name, project folder, and label file in the same window
- Frontend auto pr
- CHANGELOG.md
- Project renaming 
- Project reloading
- Project import export
- Project starring
- Project is_new label

### Changed
- Image validation method from reading files -> reading metadata
- Project creating method -> each project is only limited to one folder
- Api endpoint refactoring
- Project loading api -> v2 api
- Log file location from *{home}*/logs -> *{home}*/.classifai
- Vertx version 3.9.0 -> 4.0.2
- Database from HSQLDB -> H2
- Image UUID from incremental integer -> UUID
- Project id from incremental integer -> UUID

### Removed
- Import images from folder
- Import selected images

### Fixed
- Frontend caching issue by introducing no-cache header

## [1.2.0] - 2021-02-26
### Added
- Shortcut key page
- Malay language support

### Changed
- Chinese language interface is fully translated

### Fixed
- Load project without data that happened occasionally ([#302](https://github.com/CertifaiAI/classifai/issues/302))

## [1.1.1] - 2021-01-29
### Fixed
- Annotation output for bounding box projects changed to correspond to image name ([#277](https://github.com/CertifaiAI/classifai/issues/277)) \
  Eg. With image 1.jpg, the annotation output is 1.xml (Prior to fix: 1_jpg.xml)
- Wrong aspect ratio for JPEG image due to EXIF orientation ([#252](https://github.com/CertifaiAI/classifai/issues/252))
- Time out error for large JPEG image ([#280](https://github.com/CertifaiAI/classifai/issues/280))
- Enabling of detailed log messages if paths of images not found ([#283](https://github.com/CertifaiAI/classifai/issues/283))

## [1.1.0] - 2021-01-11
### Added
- Delete project feature
- Delete image feature
- Enable image shifting while drawing polygon in image segmentation project

### Fixed
- Forever staying in loading status while importing empty folder ([#223](https://github.com/CertifaiAI/classifai/issues/223))

## [1.0.0] - 2020-11-14
### Added
- Image bounding box labeling feature
  - Classic image classification
  - Optical character recognition
- Image segmentation labeling feature
- PDF -> JGP/PNG files format conversion\
  single file with multiple images -> one file corresponding to one image
- TIFF -> JGP/PNG files format conversion\
  single file with multiple images -> one file corresponding to one image
- Distribution for Windows(7, 8, 10), Mac, Ubuntu(18 LTS, 20 LTS) and Centos(7, 8)  

[Unreleased]: https://github.com/CertifaiAI/classifai/compare/main...v2_alpha?expand=1
[2.0.0-alpha2]: https://github.com/CertifaiAI/classifai/releases/tag/v2.0.0-alpha2
[2.0.0-alpha1]: https://github.com/CertifaiAI/classifai/releases/tag/v2.0.0-alpha1
[1.0.0]: https://github.com/CertifaiAI/classifai/releases/tag/v1.0
[1.1.0]: https://github.com/CertifaiAI/classifai/releases/tag/v1.1.0
[1.1.1]: https://github.com/CertifaiAI/classifai/releases/tag/v1.1.1
[1.2.0]: https://github.com/CertifaiAI/classifai/releases/tag/v1.2.0
