# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

The detailed rules and walkthrough of writing a changelog is located [here](https://docs.google.com/document/d/10N5B6ojby5rS7hq3cs83vqHfZgkHBU1HCKalsV2tOo0/edit#heading=h.uh2vc5aigamo)

*Note: This changelog is implemented since `2.0.0-alpha` version. All records before it are drafted based on [release notes](https://github.com/CertifaiAI/classifai/releases) might not be as detailed.*
## [Unreleased]
### Added
- CHANGELOG.md
- Project renaming 
- Project reloading
- Project versioning [WIP]
- Project import export
- Project import image from cloud storage [WIP]
- Project starring
- Project is_new label
- Database migration [WIP]

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
[1.0.0]: https://github.com/CertifaiAI/classifai/releases/tag/v1.0
[1.1.0]: https://github.com/CertifaiAI/classifai/releases/tag/v1.1.0
[1.1.1]: https://github.com/CertifaiAI/classifai/releases/tag/v1.1.1
[1.2.0]: https://github.com/CertifaiAI/classifai/releases/tag/v1.2.0