package com.tonydantona.nomad;



class ExceptionHandler implements java.lang.Thread.UncaughtExceptionHandler {
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
//        Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        String errorMsg = ex.getMessage();

    }
}

