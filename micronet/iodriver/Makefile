LDFLAGS=-lpthread
CFLAGS=-std=c99 -pthread -DUSE_THREADS -Wall -Werror

OBJECTS := iodriver.o queue.o frame.o
TARGET := iodriver



$(TARGET): $(OBJECTS)


clean:
	-rm $(OBJECTS) $(TARGET)
