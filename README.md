SSL Pinning Demo
================

Demo app to play around with SSL pinning on Android.

The user can choose to connect to several URLs:

- a regular HTTP URL (<http://example.com>),
- a regular HTTPS URL (<https://httpsnow.org>),
- an HTTPS URL with custom CA (the [CA test site](https://certs.cac.washington.edu/CAtest/) for
University of Washington).

The app is bundled with the certificate that has to be used to connect to the custom CA URL. If
the user enables SSL pinning, only that specific certificate is accepted. That means
that connections to the custom CA URL will succeed, and connections to other HTTPS hosts will
 fail.

Based on the code example at [Android Developers][android].

[android]: http://developer.android.com/training/articles/security-ssl.html#UnknownCa
