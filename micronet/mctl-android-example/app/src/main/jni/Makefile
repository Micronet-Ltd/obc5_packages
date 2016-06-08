LDFLAGS=-lpthread
CFLAGS=-std=c99 -pthread -DUSE_THREADS -Wall -Werror -g -O0

OBJECTS := mctl.o iosocket.o api.o
TARGET := mctl



$(TARGET): $(OBJECTS)


clean:
	-rm $(OBJECTS) $(TARGET)