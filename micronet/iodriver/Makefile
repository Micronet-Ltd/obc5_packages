LDFLAGS=-pthread -lpthread
CFLAGS=-std=c99 -pthread -DUSE_THREADS -Wall -g -O0

OBJECTS := iodriver.o control.o accel.o util.o tty.o queue.o frame.o
TARGET := iodriver



$(TARGET): $(OBJECTS)


clean:
	-rm $(OBJECTS) $(TARGET)
