# Changelog

All notable changes to this project will be documented in this file.

## 3.2.5 - 2023-09-20

- Fix ctx/action_time to fall back to ctx/time (instead of current time)
- Fix DV_ORDINAL and DV_SCALE conversion to support templates without value list constraints
- Update dependencies versions due to regular security updates

## 3.2.4 - 2023-06-20

- Increase dependencies versions (commons-io, jackson, kotlin)

## 3.2.3 - 2023-04-07

- Bump ehr-common dependency version to 3.2.2

## 3.2.2 - 2023-02-15

- Fix issue that participation_function attribute was not handled correctly if participation_id attribute was not present

## 3.2.1 - 2022-12-22

- Add possibility to have transient attributes
- Bump jackson dependencies version to 2.13.4
- Open web template functions for external usage
- Allow null values to be passed to builders' "with" methods
- Bump ehr-common dependency version to 3.2.1

## 3.2.0 - 2022-06-23

- Bump ehr-common dependency version to 3.2.0
- Apply changes from 3.1 branch

## 3.1.13 - 2023-06-20

- Increase dependencies versions (commons-io, jackson, kotlin)

## 3.1.12 - 2023-02-15

- Fix issue that participation_function attribute was not handled correctly if participation_id attribute was not present

## 3.1.11 - 2022-12-21

- Allow null values to be passed to builders' "with" methods

## 3.1.10 - 2022-12-01

- Open web template functions for external usage

## 3.1.9 - 2022-10-28

- Bump ehr-common dependency version to 3.1.7
- Bump jackson dependencies version to 2.13.4

## 3.1.8 - 2022-10-28

- Bump ehr-common dependency version to 3.1.5

## 3.1.7 - 2022-10-13

- Add possibility to have transient attributes

## 3.1.6 - 2022-06-13

- Add the possibility to copy ConversionContext with a different web template or AQL path
- Coded texts fields with local (template) terminology can't be created without a value through web templates

## 3.1.5 - 2022-04-07

- Add possibility to add other details to web template

## 3.1.4 - 2022-01-27

- Bump ehr-common dependency version to 3.1.4
- Deprecate LocaleBasedValueConverter

## 3.1.3 - 2021-12-06

- Bump ehr-common dependency version to 3.1.3

## 3.1.2 - 2021-10-29

- Bump ehr-common dependency version to 3.1.2

## 3.1.1 - 2021-11-26

- Bump Kotlin dependencies to 1.5.31
- Bump Jackson dependencies to 2.13.0
- Fix an issue that feeder system audit was stored in the originating system audit when the composition was converted from the STRUCTURED or FLAT format
- Fix timezone issue in tests

## 3.1.0 - 2021-10-29

- Initial release
